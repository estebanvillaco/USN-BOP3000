package com.example.planabite_backend.planabite_backend.service;

import com.example.planabite_backend.planabite_backend.model.IngredientItem;
import com.example.planabite_backend.planabite_backend.model.Meal;
import com.example.planabite_backend.planabite_backend.model.MealIngredient;
import com.example.planabite_backend.planabite_backend.model.MealPlanRequest;
import com.example.planabite_backend.planabite_backend.model.MealPlanResponse;
import com.example.planabite_backend.planabite_backend.model.MealTemplate;
import com.example.planabite_backend.planabite_backend.model.SpoonacularIngredient;
import com.example.planabite_backend.planabite_backend.model.SpoonacularRecipe;
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

    // Fallback library used when Spoonacular returns no results
    private static final List<MealTemplate> MEAL_LIBRARY = List.of(
            new MealTemplate("Kyllingfilet med ris og grønnsaker", "kyllingfilet",
                    List.of("kylling", "ris", "grønnsaker", "sunn"),
                    List.of(
                            new MealIngredient("Kyllingfilet", "600g",  "kyllingfilet",    70.0),
                            new MealIngredient("Ris",          "400g",  "ris",             20.0),
                            new MealIngredient("Frosne grønnsaker", "400g", "frosne grønnsaker", 20.0)
                    )),

            new MealTemplate("Kylling med paprika og løk i ovn", "kyllingfilet",
                    List.of("kylling", "paprika", "løk"),
                    List.of(
                            new MealIngredient("Kyllingfilet", "600g",  "kyllingfilet", 70.0),
                            new MealIngredient("Paprika",      "3 stk", "paprika",      15.0),
                            new MealIngredient("Løk",          "2 stk", "løk",          10.0)
                    )),

            new MealTemplate("Wok med kylling og grønnsaker", "kylling",
                    List.of("kylling", "wok", "grønnsaker"),
                    List.of(
                            new MealIngredient("Kylling",        "500g",     "kylling",        60.0),
                            new MealIngredient("Wok-grønnsaker", "400g",     "wok grønnsaker", 20.0),
                            new MealIngredient("Soyasaus",       "1 flaske", "soyasaus",       20.0)
                    )),

            new MealTemplate("Salat med grillet kylling og fetaost", "kylling",
                    List.of("kylling", "salat", "ost"),
                    List.of(
                            new MealIngredient("Kylling",  "400g",   "kylling",  55.0),
                            new MealIngredient("Salat",    "1 pose", "salat",    20.0),
                            new MealIngredient("Fetaost",  "200g",   "fetaost",  35.0)
                    )),

            new MealTemplate("Laksfilet med poteter og asparges", "laks",
                    List.of("laks", "fisk", "potet", "asparges", "sunn"),
                    List.of(
                            new MealIngredient("Laks",    "600g", "laks",    90.0),
                            new MealIngredient("Poteter", "1 kg", "poteter", 20.0),
                            new MealIngredient("Asparges","400g", "asparges",30.0)
                    )),

            new MealTemplate("Stekt laks med sitron og brokkoliris", "laks",
                    List.of("laks", "fisk", "sitron", "brokkoli"),
                    List.of(
                            new MealIngredient("Laks",     "600g",  "laks",    90.0),
                            new MealIngredient("Brokkoli", "500g",  "brokkoli",25.0),
                            new MealIngredient("Ris",      "400g",  "ris",     20.0),
                            new MealIngredient("Sitron",   "2 stk", "sitron",  10.0)
                    )),

            new MealTemplate("Eggerøre med laks og rømme", "egg",
                    List.of("laks", "egg", "rømme"),
                    List.of(
                            new MealIngredient("Egg",   "6 stk",  "egg",   30.0),
                            new MealIngredient("Laks",  "300g",   "laks",  60.0),
                            new MealIngredient("Rømme", "200 ml", "rømme", 20.0)
                    )),

            new MealTemplate("Pasta med kjøttdeig og tomatsaus", "pasta",
                    List.of("pasta", "kjøttdeig", "tomat"),
                    List.of(
                            new MealIngredient("Pasta",      "400g",   "pasta",     20.0),
                            new MealIngredient("Kjøttdeig",  "500g",   "kjøttdeig", 50.0),
                            new MealIngredient("Tomatsaus",  "1 boks", "tomatsaus", 15.0)
                    )),

            new MealTemplate("Pasta med paprika og karbonadedeig", "pasta",
                    List.of("pasta", "paprika", "karbonadedeig", "kjøtt"),
                    List.of(
                            new MealIngredient("Pasta",          "400g",  "pasta",          20.0),
                            new MealIngredient("Karbonadedeig",  "400g",  "karbonadedeig",  45.0),
                            new MealIngredient("Paprika",        "3 stk", "paprika",        15.0)
                    )),

            new MealTemplate("Pasta med laks og fløtesaus", "pasta",
                    List.of("pasta", "laks", "fisk", "fløte"),
                    List.of(
                            new MealIngredient("Pasta",  "400g", "pasta",  20.0),
                            new MealIngredient("Laks",   "400g", "laks",   70.0),
                            new MealIngredient("Fløte",  "2 dl", "fløte",  20.0)
                    )),

            new MealTemplate("Grønnsaksuppe med brød", "grønnsakssuppe",
                    List.of("suppe", "grønnsaker", "brød"),
                    List.of(
                            new MealIngredient("Grønnsakssuppe", "1 pk",  "grønnsakssuppe", 25.0),
                            new MealIngredient("Grønnsaker",     "400g",  "grønnsaker",     20.0),
                            new MealIngredient("Brød",           "1 stk", "brød",           25.0)
                    )),

            new MealTemplate("Tomatsuppe med fullkornsbrød", "tomatsuppe",
                    List.of("suppe", "tomat", "brød"),
                    List.of(
                            new MealIngredient("Tomatsuppe",    "1 pk", "tomatsuppe",    25.0),
                            new MealIngredient("Fullkornsbrød", "1 pk", "fullkornsbrød", 25.0)
                    )),

            new MealTemplate("Fiskesuppe med gulrot og purre", "fiskesuppe",
                    List.of("suppe", "fisk", "gulrot", "purre"),
                    List.of(
                            new MealIngredient("Fiskesuppe", "1 pk",  "fiskesuppe", 40.0),
                            new MealIngredient("Gulrot",     "4 stk", "gulrot",     10.0),
                            new MealIngredient("Purre",      "1 stk", "purre",      10.0)
                    )),

            new MealTemplate("Linsesuppe med grønnsaker", "linser",
                    List.of("suppe", "linser", "grønnsaker", "vegan"),
                    List.of(
                            new MealIngredient("Linser",  "400g",  "linser",  20.0),
                            new MealIngredient("Gulrot",  "4 stk", "gulrot",  10.0),
                            new MealIngredient("Selleri", "1 stk", "selleri", 10.0),
                            new MealIngredient("Tomat",   "4 stk", "tomat",   15.0)
                    )),

            new MealTemplate("Omelett med paprika, ost og skinke", "egg",
                    List.of("egg", "omelett", "paprika", "ost", "skinke"),
                    List.of(
                            new MealIngredient("Egg",     "6 stk", "egg",    30.0),
                            new MealIngredient("Paprika", "2 stk", "paprika",15.0),
                            new MealIngredient("Ost",     "150g",  "ost",    30.0),
                            new MealIngredient("Skinke",  "150g",  "skinke", 25.0)
                    )),

            new MealTemplate("Tacos med kjøttdeig og grønnsaker", "kjøttdeig",
                    List.of("tacos", "kjøttdeig", "kjøtt", "grønnsaker"),
                    List.of(
                            new MealIngredient("Kjøttdeig",  "500g",   "kjøttdeig",  50.0),
                            new MealIngredient("Taco-skjell","1 pk",   "taco skjell",20.0),
                            new MealIngredient("Salsa",      "1 boks", "salsa",      20.0),
                            new MealIngredient("Rømme",      "200 ml", "rømme",      20.0)
                    )),

            new MealTemplate("Tunfisksalat med paprika og mais", "tunfisk",
                    List.of("tunfisk", "salat", "paprika", "mais", "fisk"),
                    List.of(
                            new MealIngredient("Tunfisk", "2 bokser", "tunfisk", 30.0),
                            new MealIngredient("Paprika", "2 stk",    "paprika", 15.0),
                            new MealIngredient("Mais",    "1 boks",   "mais",    15.0),
                            new MealIngredient("Salat",   "1 pose",   "salat",   20.0)
                    )),

            new MealTemplate("Tofu wok med ris og grønnsaker", "tofu",
                    List.of("tofu", "ris", "grønnsaker", "vegan", "vegetar"),
                    List.of(
                            new MealIngredient("Tofu",           "400g", "tofu",           30.0),
                            new MealIngredient("Ris",            "400g", "ris",            20.0),
                            new MealIngredient("Wok-grønnsaker", "400g", "wok grønnsaker", 20.0)
                    )),

            new MealTemplate("Dampet brokkoli og kylling med hvitløk", "kyllingfilet",
                    List.of("brokkoli", "kylling", "hvitløk", "sunn"),
                    List.of(
                            new MealIngredient("Kyllingfilet", "600g",  "kyllingfilet", 70.0),
                            new MealIngredient("Brokkoli",     "600g",  "brokkoli",     25.0),
                            new MealIngredient("Hvitløk",      "1 pk",  "hvitløk",      10.0)
                    )),

            new MealTemplate("Salat med egg, tomat og agurk", "egg",
                    List.of("salat", "egg", "tomat", "agurk"),
                    List.of(
                            new MealIngredient("Egg",   "6 stk", "egg",   30.0),
                            new MealIngredient("Tomat", "4 stk", "tomat", 15.0),
                            new MealIngredient("Agurk", "1 stk", "agurk", 10.0)
                    ))
    );

    private final KassalappService kassalappService;
    private final SpoonacularService spoonacularService;
    private final IngredientTranslator translator;

    public MealPlanService(KassalappService kassalappService,
                           SpoonacularService spoonacularService,
                           IngredientTranslator translator) {
        this.kassalappService = kassalappService;
        this.spoonacularService = spoonacularService;
        this.translator = translator;
    }

    public MealPlanResponse generate(MealPlanRequest request) {
        int numDays = parseIntOrDefault(request.days(), 7);
        double budget = parseDoubleOrDefault(request.budget(), Double.MAX_VALUE);

        String query        = buildSpoonacularQuery(request.preferences(), request.goal());
        String diet         = spoonacularService.mapDietType(request.dietType());
        String intolerances = spoonacularService.mapIntolerances(request.allergies());

        log.info("Fetching Spoonacular recipes: query='{}', diet='{}', intolerances='{}'", query, diet, intolerances);
        List<SpoonacularRecipe> recipes = spoonacularService.findRecipes(query, diet, intolerances, numDays + 3);

        if (recipes.isEmpty()) {
            log.warn("Spoonacular returned no recipes for query='{}', diet='{}', intolerances='{}'", query, diet, intolerances);
            throw new IllegalStateException("Fant ingen oppskrifter fra Spoonacular. Prøv andre preferanser eller kostholdstype.");
        }

        List<Meal> meals = new ArrayList<>();
        List<IngredientItem> shoppingList = new ArrayList<>();
        double totalCost = 0;

        for (int i = 0; i < numDays; i++) {
            SpoonacularRecipe recipe = recipes.get(i % recipes.size());
            String day = DAYS[i % DAYS.length];

            List<IngredientItem> mealIngredients = new ArrayList<>();
            double mealCost = 0;

            for (SpoonacularIngredient ingredient : recipe.ingredients()) {
                String norwegianTerm = translator.toNorwegian(ingredient.name());
                double minPrice      = translator.getMinPrice(norwegianTerm);

                Meal priceResult = kassalappService.searchProduct(norwegianTerm, i + 1, day, minPrice);
                if (priceResult == null) continue;

                mealIngredients.add(new IngredientItem(
                        norwegianTerm,
                        ingredient.amount(),
                        priceResult.name(),
                        priceResult.price(),
                        priceResult.store()
                ));
                mealCost += priceResult.price();
            }

            if (mealIngredients.isEmpty()) continue;

            mealCost = Math.round(mealCost * 100.0) / 100.0;
            if (totalCost + mealCost > budget) continue;

            String mainStore = mealIngredients.get(0).store();
            meals.add(new Meal(i + 1, day, recipe.title(), mainStore, mealCost));
            shoppingList.addAll(mealIngredients);
            totalCost += mealCost;
        }

        return new MealPlanResponse(meals, shoppingList, Math.round(totalCost * 100.0) / 100.0);
    }

    private String buildSpoonacularQuery(String preferences, String goal) {
        if (preferences != null && !preferences.isBlank()) {
            return preferences.trim();
        }
        if (goal == null) return "dinner";
        return switch (goal) {
            case "healthy"     -> "healthy dinner";
            case "weight_loss" -> "low calorie meal";
            case "muscle_gain" -> "high protein meal";
            default            -> "dinner";
        };
    }

    // Fallback: generate a plan from the built-in Norwegian meal library
    private MealPlanResponse generateFromLibrary(MealPlanRequest request, int numDays, double budget) {
        List<String> keywords = resolveKeywords(request.preferences(), request.goal());

        List<Meal> meals = new ArrayList<>();
        List<IngredientItem> shoppingList = new ArrayList<>();
        double totalCost = 0;

        for (int i = 0; i < numDays; i++) {
            String keyword = keywords.get(i % keywords.size());
            String day = DAYS[i];

            MealTemplate template = findTemplate(keyword, i);

            List<IngredientItem> mealIngredients = new ArrayList<>();
            double mealCost = 0;

            for (MealIngredient ingredient : template.ingredients()) {
                Meal priceResult = kassalappService.searchProduct(ingredient.searchTerm(), i + 1, day, ingredient.minPrice());
                if (priceResult == null) continue;
                mealIngredients.add(new IngredientItem(
                        ingredient.displayName(),
                        ingredient.amount(),
                        priceResult.name(),
                        priceResult.price(),
                        priceResult.store()
                ));
                mealCost += priceResult.price();
            }

            if (mealIngredients.isEmpty()) continue;

            mealCost = Math.round(mealCost * 100.0) / 100.0;
            if (totalCost + mealCost > budget) continue;

            String mainStore = mealIngredients.get(0).store();
            meals.add(new Meal(i + 1, day, template.name(), mainStore, mealCost));
            shoppingList.addAll(mealIngredients);
            totalCost += mealCost;
        }

        return new MealPlanResponse(meals, shoppingList, Math.round(totalCost * 100.0) / 100.0);
    }

    private MealTemplate findTemplate(String keyword, int dayIndex) {
        String lc = keyword.toLowerCase().trim();
        List<MealTemplate> matches = MEAL_LIBRARY.stream()
                .filter(t -> t.tags().stream().anyMatch(tag -> tag.toLowerCase().contains(lc) || lc.contains(tag.toLowerCase())))
                .toList();

        if (!matches.isEmpty()) {
            return matches.get(dayIndex % matches.size());
        }

        return MEAL_LIBRARY.get(dayIndex % MEAL_LIBRARY.size());
    }

    private static final List<String> HEALTHY_TAGS     = List.of("kylling", "laks", "grønnsaker", "salat", "linser", "tofu", "fisk");
    private static final List<String> WEIGHT_LOSS_TAGS = List.of("salat", "suppe", "egg", "brokkoli", "grønnsaker", "kylling", "fisk");
    private static final List<String> MUSCLE_GAIN_TAGS = List.of("kylling", "egg", "laks", "kjøtt", "cottage cheese", "tunfisk", "reker");
    private static final List<String> DEFAULT_TAGS     = List.of("kylling", "pasta", "fisk", "suppe", "kjøtt", "grønnsaker", "laks");

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
    }
}
