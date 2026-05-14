package com.example.planabite_backend.planabite_backend.model;

import java.util.List;

public record RecipeResponse(
        long mealId,
        String mealName,
        int defaultServings,
        List<RecipeStep> steps,
        List<RecipeIngredient> ingredients
) {
    public record RecipeStep(int stepNumber, String instruction) {}
    public record RecipeIngredient(String name, String amount) {}
}
