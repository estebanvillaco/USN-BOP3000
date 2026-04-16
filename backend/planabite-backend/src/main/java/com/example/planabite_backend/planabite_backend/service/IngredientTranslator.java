package com.example.planabite_backend.planabite_backend.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class IngredientTranslator {

    // English ingredient name (lowercase) → Norwegian Kassalapp search term
    private static final Map<String, String> EN_TO_NO = Map.ofEntries(
            Map.entry("chicken breast",         "kyllingfilet"),
            Map.entry("chicken thighs",         "kyllinglår"),
            Map.entry("chicken thigh",          "kyllinglår"),
            Map.entry("chicken",                "kylling"),
            Map.entry("fish",                   "fisk"),
            Map.entry("white fish",             "hvitfisk"),
            Map.entry("salmon",                 "laks"),
            Map.entry("cod",                    "torsk"),
            Map.entry("shrimp",                 "reker"),
            Map.entry("prawns",                 "reker"),
            Map.entry("tuna",                   "tunfisk"),
            Map.entry("ground beef",            "kjøttdeig"),
            Map.entry("minced beef",            "kjøttdeig"),
            Map.entry("beef",                   "biff"),
            Map.entry("lamb",                   "lam"),
            Map.entry("pork",                   "svinekjøtt"),
            Map.entry("bacon",                  "bacon"),
            Map.entry("ham",                    "skinke"),
            Map.entry("egg",                    "egg"),
            Map.entry("eggs",                   "egg"),
            Map.entry("pasta",                  "pasta"),
            Map.entry("spaghetti",              "spaghetti"),
            Map.entry("rice",                   "ris"),
            Map.entry("bread",                  "brød"),
            Map.entry("baguette",               "baguette"),
            Map.entry("flour",                  "mel"),
            Map.entry("olive oil",              "olivenolje"),
            Map.entry("oil",                    "olje"),
            Map.entry("butter",                 "smør"),
            Map.entry("milk",                   "melk"),
            Map.entry("cream",                  "fløte"),
            Map.entry("heavy cream",            "fløte"),
            Map.entry("sour cream",             "rømme"),
            Map.entry("cheese",                 "ost"),
            Map.entry("feta cheese",            "fetaost"),
            Map.entry("feta",                   "fetaost"),
            Map.entry("cottage cheese",         "cottage cheese"),
            Map.entry("garlic",                 "hvitløk"),
            Map.entry("onion",                  "løk"),
            Map.entry("tomato",                 "tomat"),
            Map.entry("tomatoes",               "tomat"),
            Map.entry("tomato sauce",           "tomatsaus"),
            Map.entry("bell pepper",            "paprika"),
            Map.entry("red pepper",             "paprika"),
            Map.entry("broccoli",               "brokkoli"),
            Map.entry("carrot",                 "gulrot"),
            Map.entry("carrots",                "gulrot"),
            Map.entry("potato",                 "potet"),
            Map.entry("potatoes",               "potet"),
            Map.entry("avocado",                "avokado"),
            Map.entry("lemon",                  "sitron"),
            Map.entry("tofu",                   "tofu"),
            Map.entry("lentils",                "linser"),
            Map.entry("lentil",                 "linser"),
            Map.entry("beans",                  "bønner"),
            Map.entry("corn",                   "mais"),
            Map.entry("lettuce",                "salat"),
            Map.entry("salad",                  "salat"),
            Map.entry("cucumber",               "agurk"),
            Map.entry("mushroom",               "sopp"),
            Map.entry("mushrooms",              "sopp"),
            Map.entry("spinach",                "spinat"),
            Map.entry("asparagus",              "asparges"),
            Map.entry("celery",                 "selleri"),
            Map.entry("leek",                   "purre"),
            Map.entry("soy sauce",              "soyasaus"),
            Map.entry("salsa",                  "salsa"),
            Map.entry("chili",                  "chili"),
            Map.entry("paprika",                "paprika"),
            Map.entry("vegetable broth",        "grønnsaksbuljong"),
            Map.entry("chicken broth",          "kyllingbuljong"),
            Map.entry("frozen vegetables",      "frosne grønnsaker"),
            Map.entry("mixed vegetables",       "grønnsaker")
    );

    // Norwegian search term → minimum expected price floor in NOK
    private static final Map<String, Double> MIN_PRICES = Map.ofEntries(
            Map.entry("kyllingfilet",   70.0),
            Map.entry("kyllinglår",     60.0),
            Map.entry("kylling",        55.0),
            Map.entry("laks",           90.0),
            Map.entry("torsk",          80.0),
            Map.entry("reker",          80.0),
            Map.entry("tunfisk",        25.0),
            Map.entry("kjøttdeig",      50.0),
            Map.entry("biff",           90.0),
            Map.entry("lam",           100.0),
            Map.entry("svinekjøtt",     60.0),
            Map.entry("bacon",          40.0),
            Map.entry("skinke",         30.0),
            Map.entry("egg",            25.0),
            Map.entry("pasta",          15.0),
            Map.entry("spaghetti",      15.0),
            Map.entry("ris",            15.0),
            Map.entry("brød",           25.0),
            Map.entry("olivenolje",     40.0),
            Map.entry("smør",           30.0),
            Map.entry("melk",           15.0),
            Map.entry("fløte",          20.0),
            Map.entry("rømme",          20.0),
            Map.entry("ost",            30.0),
            Map.entry("fetaost",        30.0),
            Map.entry("cottage cheese", 25.0),
            Map.entry("tofu",           25.0),
            Map.entry("linser",         15.0),
            Map.entry("bønner",         15.0)
    );

    private static final double DEFAULT_MIN_PRICE = 10.0;

    /**
     * Returns the Norwegian Kassalapp search term for a given English ingredient name.
     * Falls back to the original name if no mapping is found.
     */
    public String toNorwegian(String englishName) {
        if (englishName == null || englishName.isBlank()) return "";
        String lc = englishName.toLowerCase().trim();

        // Exact match
        if (EN_TO_NO.containsKey(lc)) return EN_TO_NO.get(lc);

        // Partial match: check if any map key is contained in the input or vice-versa
        for (Map.Entry<String, String> entry : EN_TO_NO.entrySet()) {
            if (lc.contains(entry.getKey()) || entry.getKey().contains(lc)) {
                return entry.getValue();
            }
        }

        // No translation found — use original (Kassalapp may still find a match)
        return lc;
    }

    public double getMinPrice(String norwegianTerm) {
        return MIN_PRICES.getOrDefault(norwegianTerm, DEFAULT_MIN_PRICE);
    }
}
