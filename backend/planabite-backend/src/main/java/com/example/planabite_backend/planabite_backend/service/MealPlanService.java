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
import java.util.List;

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

        List<MealEntity> library = mealRepository.findAll();
        List<String> keywords = resolveKeywords(request.preferences(), request.goal(), request.dietType());
        List<MealEntity> candidates = filterByDiet(library, request.dietType(), request.allergies());

        List<Meal> meals = new ArrayList<>();
        List<IngredientItem> shoppingList = new ArrayList<>();
        double totalCost = 0;

        for (int i = 0; i < numDays; i++) {
            String keyword = keywords.get(i % keywords.size());
            String day = DAYS[i % DAYS.length];

            MealEntity template = findTemplate(candidates, keyword, i);
            log.info("Day {}: selected meal '{}'", i + 1, template.getName());

            List<IngredientItem> mealIngredients = new ArrayList<>();
            double mealCost = 0;

            for (MealIngredientEntity ingredient : template.getIngredients()) {
                Meal priceResult = kassalappService.searchProduct(
                        ingredient.getSearchTerm(), i + 1, day, ingredient.getMinPrice());
                if (priceResult == null) continue;

                mealIngredients.add(new IngredientItem(
                        ingredient.getDisplayName(),
                        ingredient.getAmount(),
                        priceResult.name(),
                        priceResult.price(),
                        priceResult.store(),
                        day
                ));
                mealCost += priceResult.price();
            }

            if (mealIngredients.isEmpty()) continue;

            mealCost = Math.round(mealCost * 100.0) / 100.0;
            if (totalCost + mealCost > budget) continue;

            String mainStore = mealIngredients.get(0).store();
            meals.add(new Meal(i + 1, day, template.getName(), mainStore, mealCost));
            shoppingList.addAll(mealIngredients);
            totalCost += mealCost;
        }

        return new MealPlanResponse(meals, shoppingList, Math.round(totalCost * 100.0) / 100.0);
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

    private int parseIntOrDefault(String value, int defaultValue) {
        try { return Integer.parseInt(value); } catch (NumberFormatException e) { return defaultValue; }
    }

    private double parseDoubleOrDefault(String value, double defaultValue) {
        try { return Double.parseDouble(value); } catch (NumberFormatException e) { return defaultValue; }
    }
}
