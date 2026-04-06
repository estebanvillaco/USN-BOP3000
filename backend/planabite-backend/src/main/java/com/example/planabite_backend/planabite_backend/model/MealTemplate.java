package com.example.planabite_backend.planabite_backend.model;

import java.util.List;

public record MealTemplate(
        String name,
        String searchTerm,
        List<String> tags
) {}
