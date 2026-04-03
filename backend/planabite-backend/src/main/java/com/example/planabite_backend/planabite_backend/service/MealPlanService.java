package com.example.planabite_backend.planabite_backend.service;

import com.example.planabite_backend.planabite_backend.model.Meal;
import com.example.planabite_backend.planabite_backend.model.MealPlanRequest;
import com.example.planabite_backend.planabite_backend.model.MealPlanResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class MealPlanService {

    private static final String[] DAYS = {
            "Mandag", "Tirsdag", "Onsdag", "Torsdag", "Fredag", "Lørdag", "Søndag"
    };

    private static final List<String> DEFAULT_KEYWORDS = List.of(
            "middag", "pasta", "kylling", "suppe", "fisk", "kjøtt", "grønnsaker"
    );

    private final KassalappService kassalappService;

    public MealPlanService(KassalappService kassalappService) {
        this.kassalappService = kassalappService;
    }

    public MealPlanResponse generate(MealPlanRequest request) {
        int numDays = parseIntOrDefault(request.days(), 7);
        double budget = parseDoubleOrDefault(request.budget(), Double.MAX_VALUE);
        List<String> keywords = parseKeywords(request.preferences());

        List<Meal> meals = new ArrayList<>();
        List<String> shoppingList = new ArrayList<>();
        double totalCost = 0;

        for (int i = 0; i < numDays; i++) {
            String keyword = keywords.get(i % keywords.size());
            String day = DAYS[i];

            Meal meal = kassalappService.searchProduct(keyword, i + 1, day);
            if (meal == null) continue;
            if (totalCost + meal.price() > budget) continue;

            meals.add(meal);
            shoppingList.add(meal.name());
            totalCost += meal.price();
        }

        return new MealPlanResponse(meals, shoppingList, Math.round(totalCost * 100.0) / 100.0);
    }

    private List<String> parseKeywords(String preferences) {
        if (preferences == null || preferences.isBlank()) {
            return DEFAULT_KEYWORDS;
        }
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

    private double parseDoubleOrDefault(String value, double defaultValue) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
