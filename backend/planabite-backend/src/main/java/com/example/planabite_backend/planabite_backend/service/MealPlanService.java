package com.example.planabite_backend.planabite_backend.service;

import com.example.planabite_backend.planabite_backend.model.IngredientItem;
import com.example.planabite_backend.planabite_backend.model.Meal;
import com.example.planabite_backend.planabite_backend.model.MealEntity;
import com.example.planabite_backend.planabite_backend.model.MealIngredientEntity;
import com.example.planabite_backend.planabite_backend.model.MealPlanRequest;
import com.example.planabite_backend.planabite_backend.model.MealPlanResponse;
import com.example.planabite_backend.planabite_backend.repository.MealRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MealPlanService {

    private static final Logger log = LoggerFactory.getLogger(MealPlanService.class);

    private static final String[] DAYS = {
            "Mandag", "Tirsdag", "Onsdag", "Torsdag", "Fredag", "Lørdag", "Søndag"
    };

    private final KassalappService kassalappService;
    private final MealRepository mealRepository;

    public MealPlanService(KassalappService kassalappService, MealRepository mealRepository) {
        this.kassalappService = kassalappService;
        this.mealRepository = mealRepository;
    }

    public MealPlanResponse generate(MealPlanRequest request) {
        int numDays = parseIntOrDefault(request.days(), 7);
        double budget = parseDoubleOrDefault(request.budget(), Double.MAX_VALUE);
        int requestedServings = parseIntOrDefault(request.servings(), 0); // 0 = use each meal's default

        List<MealEntity> library = mealRepository.findAll();
        List<String> keywords = resolveKeywords(request.preferences(), request.goal(), request.dietType());
        List<MealEntity> candidates = filterByDiet(library, request.dietType(), request.allergies());

        if ("cheapest".equals(request.preferredStore())) {
            return generateWithCheapestStore(numDays, budget, keywords, candidates, requestedServings);
        }

        List<Meal> meals = new ArrayList<>();
        List<IngredientItem> shoppingList = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        double totalCost = 0;

        for (int i = 0; i < numDays; i++) {
            String keyword = keywords.get(i % keywords.size());
            String day = DAYS[i % DAYS.length];

            MealEntity template = findTemplate(candidates, keyword, i);
            log.info("Day {}: selected meal '{}'", i + 1, template.getName());

            int effectiveServings = requestedServings > 0 ? requestedServings : template.getDefaultServings();
            double scale = (double) effectiveServings / template.getDefaultServings();

            List<IngredientItem> mealIngredients = new ArrayList<>();
            double mealCost = 0;

            for (MealIngredientEntity ingredient : template.getIngredients()) {
                Meal priceResult = kassalappService.searchProduct(
                        ingredient.getSearchTerm(), i + 1, day, ingredient.getMinPrice(), request.preferredStore());
                if (priceResult == null) {
                    warnings.add(ingredient.getDisplayName() + " ble ikke funnet i butikken og mangler i handlelisten.");
                    continue;
                }
                mealIngredients.add(new IngredientItem(
                        ingredient.getDisplayName(), scaleAmount(ingredient.getAmount(), scale),
                        priceResult.name(), priceResult.price(), priceResult.store(), day));
                mealCost += priceResult.price();
            }

            if (mealIngredients.isEmpty()) continue;

            mealCost = Math.round(mealCost * 100.0) / 100.0;
            if (totalCost + mealCost > budget) continue;

            String mainStore = mealIngredients.get(0).store();
            meals.add(new Meal(template.getId().intValue(), day, template.getName(), mainStore, mealCost, effectiveServings));
            shoppingList.addAll(mealIngredients);
            totalCost += mealCost;
        }

        return new MealPlanResponse(meals, consolidateShoppingList(shoppingList), Math.round(totalCost * 100.0) / 100.0, warnings);
    }

    /**
     * Two-pass generation for "cheapest store" mode:
     * Pass 1 — fetch all ingredient prices across all stores for every meal.
     * Pass 2 — pick the single store with best global coverage + lowest total, then build all meals from it.
     */
    private MealPlanResponse generateWithCheapestStore(int numDays, double budget,
                                                        List<String> keywords, List<MealEntity> candidates,
                                                        int requestedServings) {
        record MealSnapshot(MealEntity template, String day, int index,
                            List<java.util.Map<String, Meal>> ingredientsByStore) {}

        // Pass 1: collect ingredient data for every day across all stores
        List<MealSnapshot> snapshots = new ArrayList<>();
        java.util.Map<String, Double>  globalCosts    = new java.util.LinkedHashMap<>();
        java.util.Map<String, Integer> globalCoverage = new java.util.LinkedHashMap<>();

        for (int i = 0; i < numDays; i++) {
            String keyword = keywords.get(i % keywords.size());
            String day = DAYS[i % DAYS.length];
            MealEntity template = findTemplate(candidates, keyword, i);
            log.info("Day {}: selected meal '{}' (cheapest-store pass 1)", i + 1, template.getName());

            List<java.util.Map<String, Meal>> ingredientsByStore = new ArrayList<>();
            for (MealIngredientEntity ingredient : template.getIngredients()) {
                java.util.Map<String, Meal> byStore = kassalappService.searchProductAllStores(
                        ingredient.getSearchTerm(), i + 1, day, ingredient.getMinPrice());
                ingredientsByStore.add(byStore);
                byStore.forEach((store, meal) -> {
                    globalCosts.merge(store, meal.price(), Double::sum);
                    globalCoverage.merge(store, 1, Integer::sum);
                });
            }
            snapshots.add(new MealSnapshot(template, day, i, ingredientsByStore));
        }

        // Find the one store with the highest ingredient coverage, tiebroken by lowest total cost
        int maxCoverage = globalCoverage.values().stream().mapToInt(v -> v).max().orElse(0);
        String bestStore = globalCoverage.entrySet().stream()
                .filter(e -> e.getValue() == maxCoverage)
                .min(java.util.Comparator.comparingDouble(e -> globalCosts.getOrDefault(e.getKey(), Double.MAX_VALUE)))
                .map(java.util.Map.Entry::getKey)
                .orElse(null);

        if (bestStore == null) return new MealPlanResponse(List.of(), List.of(), 0, List.of());
        log.info("Globally cheapest store across all meals: '{}'", bestStore);

        // Pass 2: build meals using only the chosen store
        List<Meal> meals = new ArrayList<>();
        List<IngredientItem> shoppingList = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        double totalCost = 0;

        for (MealSnapshot snap : snapshots) {
            int effectiveServings = requestedServings > 0 ? requestedServings : snap.template().getDefaultServings();
            double scale = (double) effectiveServings / snap.template().getDefaultServings();

            List<IngredientItem> mealIngredients = new ArrayList<>();
            double mealCost = 0;

            List<MealIngredientEntity> templateIngredients = snap.template().getIngredients();
            for (int j = 0; j < templateIngredients.size(); j++) {
                MealIngredientEntity ingredient = templateIngredients.get(j);
                Meal priceResult = snap.ingredientsByStore().get(j).get(bestStore);
                if (priceResult == null) {
                    warnings.add(ingredient.getDisplayName() + " ble ikke funnet i butikken og mangler i handlelisten.");
                    continue;
                }
                mealIngredients.add(new IngredientItem(
                        ingredient.getDisplayName(), scaleAmount(ingredient.getAmount(), scale),
                        priceResult.name(), priceResult.price(), bestStore, snap.day()));
                mealCost += priceResult.price();
            }

            if (mealIngredients.isEmpty()) continue;
            mealCost = Math.round(mealCost * 100.0) / 100.0;
            if (totalCost + mealCost > budget) continue;

            meals.add(new Meal(snap.template().getId().intValue(), snap.day(), snap.template().getName(), bestStore, mealCost, effectiveServings));
            shoppingList.addAll(mealIngredients);
            totalCost += mealCost;
        }

        return new MealPlanResponse(meals, consolidateShoppingList(shoppingList), Math.round(totalCost * 100.0) / 100.0, warnings);
    }

    private List<MealEntity> filterByDiet(List<MealEntity> library, String dietType, String allergies) {
        List<MealEntity> result = new ArrayList<>(library);

        if (dietType != null) {
            String lc = dietType.toLowerCase();
            if (lc.contains("vegan")) {
                result = result.stream()
                        .filter(e -> parseTags(e).contains("vegan"))
                        .toList();
            } else if (lc.contains("vegetar")) {
                result = result.stream()
                        .filter(e -> { List<String> tags = parseTags(e); return tags.contains("vegan") || tags.contains("vegetar"); })
                        .toList();
            }
        }

        if (allergies != null && !allergies.isBlank()) {
            String lc = allergies.toLowerCase();
            if (lc.contains("fisk") || lc.contains("seafood")) {
                result = result.stream()
                        .filter(e -> parseTags(e).stream().noneMatch(tag ->
                                tag.equals("fisk") || tag.equals("laks") || tag.equals("tunfisk")))
                        .toList();
            }
            if (lc.contains("egg")) {
                result = result.stream()
                        .filter(e -> parseTags(e).stream().noneMatch(tag -> tag.equals("egg")))
                        .toList();
            }
        }

        return result.isEmpty() ? library : result;
    }

    private MealEntity findTemplate(List<MealEntity> candidates, String keyword, int dayIndex) {
        String lc = keyword.toLowerCase().trim();
        List<MealEntity> matches = candidates.stream()
                .filter(e -> parseTags(e).stream().anyMatch(tag ->
                        tag.toLowerCase().contains(lc) || lc.contains(tag.toLowerCase())))
                .toList();

        return !matches.isEmpty()
                ? matches.get(dayIndex % matches.size())
                : candidates.get(dayIndex % candidates.size());
    }

    private List<String> parseTags(MealEntity entity) {
        List<String> tags = new ArrayList<>();
        if (entity.getDietType() != null)
            tags.addAll(Arrays.asList(entity.getDietType().split(",")));
        if (entity.getGoal() != null)
            tags.addAll(Arrays.asList(entity.getGoal().split(",")));
        if (entity.getTags() != null)
            tags.addAll(Arrays.asList(entity.getTags().split(",")));
        return tags.stream().map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    private static final List<String> HEALTHY_TAGS     = List.of("kylling", "laks", "grønnsaker", "salat", "linser", "tofu", "fisk");
    private static final List<String> WEIGHT_LOSS_TAGS = List.of("salat", "suppe", "egg", "brokkoli", "grønnsaker", "kylling", "fisk");
    private static final List<String> MUSCLE_GAIN_TAGS = List.of("kylling", "egg", "laks", "kjøtt", "tunfisk");
    private static final List<String> VEGAN_TAGS       = List.of("vegan", "grønnsaker", "linser", "tofu", "suppe");
    private static final List<String> DEFAULT_TAGS     = List.of("kylling", "pasta", "fisk", "suppe", "kjøtt", "grønnsaker", "laks");

    private List<String> resolveKeywords(String preferences, String goal, String dietType) {
        if (preferences != null && !preferences.isBlank()) {
            List<String> parsed = Arrays.stream(preferences.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
            if (!parsed.isEmpty()) return parsed;
        }

        if (dietType != null) {
            String lc = dietType.toLowerCase();
            if (lc.contains("vegan")) return VEGAN_TAGS;
            if (lc.contains("vegetar")) return VEGAN_TAGS;
        }

        if (goal == null) return DEFAULT_TAGS;
        return switch (goal) {
            case "healthy"     -> HEALTHY_TAGS;
            case "weight_loss" -> WEIGHT_LOSS_TAGS;
            case "muscle_gain" -> MUSCLE_GAIN_TAGS;
            default            -> DEFAULT_TAGS;
        };
    }

    private List<IngredientItem> consolidateShoppingList(List<IngredientItem> items) {
        Map<String, IngredientItem> consolidated = new LinkedHashMap<>();
        for (IngredientItem item : items) {
            String key = item.name().toLowerCase() + "|" + item.store();
            IngredientItem existing = consolidated.get(key);
            if (existing == null) {
                consolidated.put(key, item);
            } else {
                double newPrice = Math.round((existing.price() + item.price()) * 100.0) / 100.0;
                String newAmount = sumAmounts(existing.amount(), item.amount());
                String newDay = mergeDays(existing.day(), item.day());
                consolidated.put(key, new IngredientItem(existing.name(), newAmount, existing.productName(), newPrice, existing.store(), newDay));
            }
        }
        return new ArrayList<>(consolidated.values());
    }

    private static String sumAmounts(String a, String b) {
        if (a == null || a.isBlank()) return b;
        if (b == null || b.isBlank()) return a;
        Matcher ma = AMOUNT_PATTERN.matcher(a.trim());
        Matcher mb = AMOUNT_PATTERN.matcher(b.trim());
        if (ma.matches() && mb.matches()) {
            String unitA = ma.group(2).trim();
            String unitB = mb.group(2).trim();
            if (unitA.equalsIgnoreCase(unitB)) {
                double sum = parseNumPart(ma.group(1)) + parseNumPart(mb.group(1));
                String formatted = (sum % 1.0 == 0.0) ? String.valueOf((int) sum) : String.format("%.1f", sum);
                return unitA.isEmpty() ? formatted : formatted + " " + unitA;
            }
        }
        return a + " + " + b;
    }

    private static String mergeDays(String existing, String incoming) {
        if (existing == null || existing.isBlank()) return incoming;
        if (incoming == null || incoming.isBlank()) return existing;
        if (existing.contains(incoming)) return existing;
        return existing + ", " + incoming;
    }

    private static final Pattern AMOUNT_PATTERN = Pattern.compile("^(\\d+(?:/\\d+)?(?:\\.\\d+)?)\\s*(.*)$");

    static String scaleAmount(String amount, double scale) {
        if (amount == null || amount.isBlank() || scale == 1.0) return amount;
        Matcher m = AMOUNT_PATTERN.matcher(amount.trim());
        if (!m.matches()) return amount;
        double scaled = parseNumPart(m.group(1)) * scale;
        String unit = m.group(2);
        String formatted = (scaled % 1.0 == 0.0)
                ? String.valueOf((int) scaled)
                : String.format("%.1f", scaled);
        return (unit != null && !unit.isEmpty()) ? formatted + " " + unit : formatted;
    }

    private static double parseNumPart(String numPart) {
        if (numPart.contains("/")) {
            String[] parts = numPart.split("/");
            return Double.parseDouble(parts[0]) / Double.parseDouble(parts[1]);
        }
        return Double.parseDouble(numPart);
    }

    private int parseIntOrDefault(String value, int defaultValue) {
        try { return Integer.parseInt(value); } catch (NumberFormatException e) { return defaultValue; }
    }

    private double parseDoubleOrDefault(String value, double defaultValue) {
        try { return Double.parseDouble(value); } catch (NumberFormatException e) { return defaultValue; }
    }
}
