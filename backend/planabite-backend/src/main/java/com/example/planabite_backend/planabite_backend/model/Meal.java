package com.example.planabite_backend.planabite_backend.model;

<<<<<<< HEAD
=======
import java.util.List;

>>>>>>> f19a9760d6658d1b444331042040346fb576e4b2
public record Meal(
        int id,
        String day,
        String name,
        String store,
<<<<<<< HEAD
        double price
=======
        double price,
        List<IngredientItem> ingredients
>>>>>>> f19a9760d6658d1b444331042040346fb576e4b2
) {}
