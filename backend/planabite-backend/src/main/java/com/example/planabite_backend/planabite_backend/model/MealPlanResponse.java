package com.example.planabite_backend.planabite_backend.model;

import java.util.List;

public record MealPlanResponse(
        List<Meal> meals,
        List<String> shoppingList,
<<<<<<< HEAD
        double totalCost
=======
        double totalCost,
        double budget
>>>>>>> f19a9760d6658d1b444331042040346fb576e4b2
) {}
