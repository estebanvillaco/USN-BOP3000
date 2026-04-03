package com.example.planabite_backend.planabite_backend.service;

import com.example.planabite_backend.planabite_backend.model.IngredientItem;
import com.example.planabite_backend.planabite_backend.model.Meal;
import com.example.planabite_backend.planabite_backend.model.MealPlanRequest;
import com.example.planabite_backend.planabite_backend.model.MealPlanResponse;
import com.example.planabite_backend.planabite_backend.model.SpoonacularRecipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MealPlanService {

    private static final Logger log = LoggerFactory.getLogger(MealPlanService.class);

    private static final String[] DAYS = {
            "Mandag", "Tirsdag", "Onsdag", "Torsdag", "Fredag", "Lørdag", "Søndag"
    };

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
    }

    public MealPlanResponse generate(MealPlanRequest request) {
        int numDays = parseIntOrDefault(request.days(), 7);
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
        double totalCost = 0;

        for (int i = 0; i < numDays; i++) {
            String keyword = keywords.get(i % keywords.size());
            String day = DAYS[i];

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
    }
}
