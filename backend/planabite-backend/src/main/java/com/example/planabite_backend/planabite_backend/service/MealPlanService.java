package com.example.planabite_backend.planabite_backend.service;

<<<<<<< HEAD
import com.example.planabite_backend.planabite_backend.model.Meal;
import com.example.planabite_backend.planabite_backend.model.MealPlanRequest;
import com.example.planabite_backend.planabite_backend.model.MealPlanResponse;
import com.example.planabite_backend.planabite_backend.model.MealTemplate;
=======
import com.example.planabite_backend.planabite_backend.model.IngredientItem;
import com.example.planabite_backend.planabite_backend.model.Meal;
import com.example.planabite_backend.planabite_backend.model.MealPlanRequest;
import com.example.planabite_backend.planabite_backend.model.MealPlanResponse;
import com.example.planabite_backend.planabite_backend.model.SpoonacularRecipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
>>>>>>> f19a9760d6658d1b444331042040346fb576e4b2
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
<<<<<<< HEAD
=======
import java.util.Map;
import java.util.stream.Collectors;
>>>>>>> f19a9760d6658d1b444331042040346fb576e4b2

@Service
public class MealPlanService {

<<<<<<< HEAD
=======
    private static final Logger log = LoggerFactory.getLogger(MealPlanService.class);

>>>>>>> f19a9760d6658d1b444331042040346fb576e4b2
    private static final String[] DAYS = {
            "Mandag", "Tirsdag", "Onsdag", "Torsdag", "Fredag", "Lørdag", "Søndag"
    };

<<<<<<< HEAD
    /**
     * Full meal template library.
     * - name:       the full dish name shown to the user
     * - searchTerm: what is sent to Kassalapp to get a real price
     * - tags:       ingredient/keyword tokens used to match user preferences
     */
    private static final List<MealTemplate> MEAL_LIBRARY = List.of(
            new MealTemplate("Kyllingfilet med ris og grønnsaker",        "kyllingfilet",    List.of("kylling", "ris", "grønnsaker", "sunn")),
            new MealTemplate("Kylling med paprika og løk i ovn",          "kyllingfilet",    List.of("kylling", "paprika", "løk")),
            new MealTemplate("Wok med kylling og grønnsaker",             "kylling",         List.of("kylling", "wok", "grønnsaker")),
            new MealTemplate("Salat med grillet kylling og fetaost",      "kylling",         List.of("kylling", "salat", "ost")),

            new MealTemplate("Laksfilet med poteter og asparges",         "laks",            List.of("laks", "fisk", "potet", "asparges", "sunn")),
            new MealTemplate("Stekt laks med sitron og brokkoliris",      "laks",            List.of("laks", "fisk", "sitron", "brokkoli")),
            new MealTemplate("Eggerøre med laks og rømme",                "egg",             List.of("laks", "egg", "rømme")),

            new MealTemplate("Pasta med kjøttdeig og tomatsaus",          "pasta",           List.of("pasta", "kjøttdeig", "tomat")),
            new MealTemplate("Pasta med paprika og karbonadedeig",        "pasta",           List.of("pasta", "paprika", "karbonadedeig", "kjøtt")),
            new MealTemplate("Pasta med laks og fløtesaus",               "pasta",           List.of("pasta", "laks", "fisk", "fløte")),

            new MealTemplate("Baguette med skinke, ost og paprika",       "baguette",        List.of("baguette", "brød", "skinke", "ost", "paprika")),
            new MealTemplate("Fullkornsbrød med avokado og egg",          "egg",             List.of("brød", "fullkorn", "avokado", "egg")),
            new MealTemplate("Pizzabrød med tomatsaus og ost",            "pizzabrød",       List.of("pizza", "brød", "ost", "tomat")),

            new MealTemplate("Grønnsaksuppe med brød",                    "grønnsakssuppe",  List.of("suppe", "grønnsaker", "brød")),
            new MealTemplate("Tomatsuppe med fullkornsbrød",              "tomatsuppe",      List.of("suppe", "tomat", "brød")),
            new MealTemplate("Fiskesuppe med gulrot og purre",            "fiskesuppe",      List.of("suppe", "fisk", "gulrot", "purre")),
            new MealTemplate("Linsesuppe med grønnsaker",                 "linser",          List.of("suppe", "linser", "grønnsaker", "vegan")),
            new MealTemplate("Bønnesuppe med fullkornsbrød",              "bønner",          List.of("suppe", "bønner", "fullkorn", "vegan")),

            new MealTemplate("Omelett med paprika, ost og skinke",        "egg",             List.of("egg", "omelett", "paprika", "ost", "skinke")),
            new MealTemplate("Eggerøre med tomat og urter",               "egg",             List.of("egg", "tomat", "urter")),

            new MealTemplate("Tacos med kjøttdeig og grønnsaker",         "kjøttdeig",       List.of("tacos", "kjøttdeig", "kjøtt", "grønnsaker")),
            new MealTemplate("Kjøttkaker med brun saus og poteter",       "kjøttkaker",      List.of("kjøtt", "kjøttkaker", "potet", "saus")),
            new MealTemplate("Lammekjøtt med rotgrønnsaker",              "lam",             List.of("lam", "kjøtt", "rotgrønnsaker")),

            new MealTemplate("Tunfisksalat med paprika og mais",          "tunfisk",         List.of("tunfisk", "salat", "paprika", "mais", "fisk")),
            new MealTemplate("Rekesalat med avokado og sitron",           "reker",           List.of("reker", "sjømat", "salat", "avokado")),

            new MealTemplate("Cottage cheese med frukt og nøtter",        "cottage cheese",  List.of("cottage cheese", "protein", "frukt")),
            new MealTemplate("Tofu wok med ris og grønnsaker",            "tofu",            List.of("tofu", "ris", "grønnsaker", "vegan", "vegetar")),

            new MealTemplate("Dampet brokkoli og kylling med hvitløk",    "kyllingfilet",    List.of("brokkoli", "kylling", "hvitløk", "sunn")),
            new MealTemplate("Salat med egg, tomat og agurk",             "egg",             List.of("salat", "egg", "tomat", "agurk"))
    );

    /**
     * Multiplier applied to the main-ingredient price returned by Kassalapp.
     * A single Kassalapp search only finds one ingredient. A real meal includes
     * vegetables, starch, oil, spices, etc. 2.5× gives a realistic total estimate.
     */
    private static final double MEAL_COST_FACTOR = 2.5;

    // Goal-based fallback keyword lists (used when user enters no preferences)
    private static final List<String> HEALTHY_TAGS     = List.of("kylling", "laks", "grønnsaker", "salat", "linser", "tofu", "fisk");
    private static final List<String> WEIGHT_LOSS_TAGS = List.of("salat", "suppe", "egg", "brokkoli", "grønnsaker", "kylling", "fisk");
    private static final List<String> MUSCLE_GAIN_TAGS = List.of("kylling", "egg", "laks", "kjøtt", "cottage cheese", "tunfisk", "reker");
    private static final List<String> DEFAULT_TAGS     = List.of("kylling", "pasta", "fisk", "suppe", "kjøtt", "grønnsaker", "laks");

    private final KassalappService kassalappService;

    public MealPlanService(KassalappService kassalappService) {
        this.kassalappService = kassalappService;
=======
    // Fallback keywords used when Spoonacular returns no results
    private static final List<String> DEFAULT_KEYWORDS = List.of(
            "middag", "pasta", "kylling", "suppe", "fisk", "kjøtt", "grønnsaker"
    );
    private static final List<String> KEYWORDS_VEGETARIAN = List.of(
            "pasta", "grønnsaker", "tofu", "egg", "ost", "linser", "bønner", "suppe"
    );
    private static final List<String> KEYWORDS_VEGAN = List.of(
            "grønnsaker", "tofu", "linser", "bønner", "havregrøt", "nøtter", "frukt"
    );
    private static final List<String> KEYWORDS_LACTOSE_FREE = List.of(
            "kylling", "fisk", "pasta", "ris", "grønnsaker", "kjøtt", "suppe"
    );
    private static final List<String> KEYWORDS_GLUTEN_FREE = List.of(
            "kylling", "fisk", "ris", "grønnsaker", "poteter", "kjøtt", "egg"
    );

    private final KassalappService kassalappService;
    private final SpoonacularService spoonacularService;

    public MealPlanService(KassalappService kassalappService, SpoonacularService spoonacularService) {
        this.kassalappService = kassalappService;
        this.spoonacularService = spoonacularService;
>>>>>>> f19a9760d6658d1b444331042040346fb576e4b2
    }

    public MealPlanResponse generate(MealPlanRequest request) {
        int numDays = parseIntOrDefault(request.days(), 7);
<<<<<<< HEAD
        double budget = parseDoubleOrDefault(request.budget(), Double.MAX_VALUE);
        List<String> keywords = resolveKeywords(request.preferences(), request.goal());

        List<Meal> meals = new ArrayList<>();
        List<String> shoppingList = new ArrayList<>();
=======
        double budget = request.budget();
        List<String> allergyTerms = buildAllergyTerms(request.allergies());

        String query = (request.preferences() != null && !request.preferences().isBlank())
                ? request.preferences() : "";
        String diet = spoonacularService.mapDietType(request.dietType());
        String intolerances = spoonacularService.mapIntolerances(request.allergies());

        List<SpoonacularRecipe> recipes = spoonacularService.findRecipes(query, diet, intolerances, numDays);

        if (!recipes.isEmpty()) {
            return generateFromRecipes(recipes, numDays, budget, allergyTerms, request.budget());
        }

        log.warn("Spoonacular returned no recipes, falling back to keyword-based generation");
        return generateFallback(request, numDays, budget, allergyTerms);
    }

    private MealPlanResponse generateFromRecipes(List<SpoonacularRecipe> recipes, int numDays,
                                                  double budget, List<String> allergyTerms, double originalBudget) {
        List<Meal> meals = new ArrayList<>();
        double totalCost = 0;
        int mealId = 1;

        for (int i = 0; i < Math.min(recipes.size(), numDays); i++) {
            SpoonacularRecipe recipe = recipes.get(i);
            String day = DAYS[i];

            List<IngredientItem> foundIngredients = new ArrayList<>();
            for (String ingredientName : recipe.ingredients()) {
                IngredientItem item = kassalappService.searchIngredient(ingredientName);
                if (item == null) continue;
                if (containsAllergenItem(item, allergyTerms)) continue;
                foundIngredients.add(item);
            }

            if (foundIngredients.isEmpty()) {
                log.warn("No ingredients found for recipe '{}', skipping", recipe.title());
                continue;
            }

            double mealPrice = foundIngredients.stream()
                    .mapToDouble(IngredientItem::price)
                    .sum();
            mealPrice = Math.round(mealPrice * 100.0) / 100.0;

            if (totalCost + mealPrice > budget) {
                log.info("Recipe '{}' exceeds remaining budget, skipping", recipe.title());
                continue;
            }

            String store = mostCommonStore(foundIngredients);
            meals.add(new Meal(mealId++, day, recipe.title(), store, mealPrice, foundIngredients));
            totalCost += mealPrice;
        }

        List<String> shoppingList = meals.stream()
                .flatMap(m -> m.ingredients().stream())
                .map(IngredientItem::name)
                .distinct()
                .toList();

        return new MealPlanResponse(meals, shoppingList, Math.round(totalCost * 100.0) / 100.0, originalBudget);
    }

    private MealPlanResponse generateFallback(MealPlanRequest request, int numDays,
                                               double budget, List<String> allergyTerms) {
        List<String> keywords = buildKeywordPool(request);
        List<Meal> meals = new ArrayList<>();
>>>>>>> f19a9760d6658d1b444331042040346fb576e4b2
        double totalCost = 0;

        for (int i = 0; i < numDays; i++) {
            String keyword = keywords.get(i % keywords.size());
            String day = DAYS[i];

<<<<<<< HEAD
            MealTemplate template = findTemplate(keyword, i);
            Meal priceResult = kassalappService.searchProduct(template.searchTerm(), i + 1, day);
            if (priceResult == null) continue;

            // Scale up from single-ingredient price to estimated full meal cost
            double estimatedMealCost = Math.round(priceResult.price() * MEAL_COST_FACTOR * 100.0) / 100.0;
            if (totalCost + estimatedMealCost > budget) continue;

            // Use the template's proper meal name, but the real store and estimated meal cost
            Meal meal = new Meal(i + 1, day, template.name(), priceResult.store(), estimatedMealCost);
            meals.add(meal);
            shoppingList.add(meal.name());
            totalCost += meal.price();
        }

        return new MealPlanResponse(meals, shoppingList, Math.round(totalCost * 100.0) / 100.0);
    }

    /**
     * Find a meal template whose tags contain the given keyword.
     * The dayIndex is used to cycle among multiple matches for variety.
     * Falls back to a generic template if nothing matches.
     */
    private MealTemplate findTemplate(String keyword, int dayIndex) {
        String lc = keyword.toLowerCase().trim();
        List<MealTemplate> matches = MEAL_LIBRARY.stream()
                .filter(t -> t.tags().stream().anyMatch(tag -> tag.toLowerCase().contains(lc) || lc.contains(tag.toLowerCase())))
                .toList();

        if (!matches.isEmpty()) {
            return matches.get(dayIndex % matches.size());
        }

        // No tag match — return a generic balanced meal
        return MEAL_LIBRARY.get(dayIndex % MEAL_LIBRARY.size());
    }

    private List<String> resolveKeywords(String preferences, String goal) {
        if (preferences != null && !preferences.isBlank()) {
            List<String> parsed = Arrays.stream(preferences.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
            if (!parsed.isEmpty()) return parsed;
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
=======
            IngredientItem item = kassalappService.searchIngredient(keyword);
            if (item == null) continue;
            if (containsAllergenItem(item, allergyTerms)) continue;
            if (totalCost + item.price() > budget) continue;

            List<IngredientItem> ingredients = List.of(item);
            meals.add(new Meal(i + 1, day, item.name(), item.store(), item.price(), ingredients));
            totalCost += item.price();
        }

        List<String> shoppingList = meals.stream()
                .flatMap(m -> m.ingredients().stream())
                .map(IngredientItem::name)
                .distinct()
                .toList();

        return new MealPlanResponse(meals, shoppingList, Math.round(totalCost * 100.0) / 100.0, budget);
    }

    private String mostCommonStore(List<IngredientItem> ingredients) {
        return ingredients.stream()
                .collect(Collectors.groupingBy(IngredientItem::store, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Varierende butikker");
    }

    private boolean containsAllergenItem(IngredientItem item, List<String> allergyTerms) {
        if (allergyTerms.isEmpty()) return false;
        String nameLower = item.name().toLowerCase();
        for (String term : allergyTerms) {
            if (nameLower.contains(term)) return true;
        }
        return false;
    }

    private List<String> buildAllergyTerms(String allergies) {
        if (allergies == null || allergies.isBlank()) return List.of();
        return Arrays.stream(allergies.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private List<String> buildKeywordPool(MealPlanRequest request) {
        if (request.preferences() != null && !request.preferences().isBlank()) {
            return parseKeywords(request.preferences());
        }
        if (request.dietType() == null) return DEFAULT_KEYWORDS;
        return switch (request.dietType()) {
            case "Vegetarian" -> KEYWORDS_VEGETARIAN;
            case "Vegan" -> KEYWORDS_VEGAN;
            case "Lactose Free" -> KEYWORDS_LACTOSE_FREE;
            case "Gluten Free" -> KEYWORDS_GLUTEN_FREE;
            default -> DEFAULT_KEYWORDS;
        };
    }

    private List<String> parseKeywords(String preferences) {
        List<String> keywords = Arrays.stream(preferences.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        return keywords.isEmpty() ? DEFAULT_KEYWORDS : keywords;
    }

    private int parseIntOrDefault(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
>>>>>>> f19a9760d6658d1b444331042040346fb576e4b2
    }
}
