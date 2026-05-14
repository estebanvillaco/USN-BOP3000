package com.example.planabite_backend.planabite_backend.controller;

import com.example.planabite_backend.planabite_backend.model.MealEntity;
import com.example.planabite_backend.planabite_backend.model.MealRecipeEntity;
import com.example.planabite_backend.planabite_backend.model.RecipeResponse;
import com.example.planabite_backend.planabite_backend.repository.MealRecipeRepository;
import com.example.planabite_backend.planabite_backend.repository.MealRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RecipeController {

    private final MealRepository mealRepository;
    private final MealRecipeRepository mealRecipeRepository;

    public RecipeController(MealRepository mealRepository, MealRecipeRepository mealRecipeRepository) {
        this.mealRepository = mealRepository;
        this.mealRecipeRepository = mealRecipeRepository;
    }

    @GetMapping("/meal/{id}/recipe")
    public ResponseEntity<RecipeResponse> getRecipe(@PathVariable Long id) {
        MealEntity meal = mealRepository.findById(id).orElse(null);
        if (meal == null) return ResponseEntity.notFound().build();

        List<MealRecipeEntity> steps = mealRecipeRepository.findByMealIdOrderByStepNumber(id);

        RecipeResponse response = new RecipeResponse(
                meal.getId(),
                meal.getName(),
                meal.getDefaultServings(),
                steps.stream()
                        .map(s -> new RecipeResponse.RecipeStep(s.getStepNumber(), s.getInstruction()))
                        .toList(),
                meal.getIngredients().stream()
                        .map(i -> new RecipeResponse.RecipeIngredient(i.getDisplayName(), i.getAmount()))
                        .toList()
        );

        return ResponseEntity.ok(response);
    }
}
