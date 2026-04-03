package com.example.planabite_backend.planabite_backend.model;

public record Meal(
        int id,
        String day,
        String name,
        String store,
        double price
) {}
