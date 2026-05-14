package com.example.planabite_backend.planabite_backend.repository;

import com.example.planabite_backend.planabite_backend.model.MealRecipeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MealRecipeRepository extends JpaRepository<MealRecipeEntity, Long> {
    List<MealRecipeEntity> findByMealIdOrderByStepNumber(Long mealId);
}
