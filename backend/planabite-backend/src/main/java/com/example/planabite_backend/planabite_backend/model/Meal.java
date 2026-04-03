package com.example.planabite_backend.planabite_backend.model;

import java.util.List;

public record Meal(
        int id,
        String day,
        String name,
        String store,
        double price,
        List<IngredientItem> ingredients
) {}
