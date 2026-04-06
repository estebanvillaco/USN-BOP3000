package com.example.planabite_backend.planabite_backend.model;

public record MealPlanRequest(
        String budget,
        String days,
        String dietType,
        String allergies,
        String preferences,
        String goal
) {}
