package com.example.planabite_backend.planabite_backend.model;

public record MealPlanRequest(
        double budget,
        String days,
        String dietType,
        String allergies,
        String preferences
) {}
