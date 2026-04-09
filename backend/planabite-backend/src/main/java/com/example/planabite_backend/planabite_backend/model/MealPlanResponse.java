package com.example.planabite_backend.planabite_backend.model;

import java.util.List;

public record MealPlanResponse(
        List<Meal> meals,
        List<IngredientItem> shoppingList,
        double totalCost
) {}
