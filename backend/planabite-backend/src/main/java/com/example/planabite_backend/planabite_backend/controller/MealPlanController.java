package com.example.planabite_backend.planabite_backend.controller;

import com.example.planabite_backend.planabite_backend.model.MealPlanRequest;
import com.example.planabite_backend.planabite_backend.model.MealPlanResponse;
import com.example.planabite_backend.planabite_backend.service.MealPlanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class MealPlanController {

    private final MealPlanService mealPlanService;

    public MealPlanController(MealPlanService mealPlanService) {
        this.mealPlanService = mealPlanService;
    }

    @PostMapping("/meal-plan")
    public ResponseEntity<MealPlanResponse> generateMealPlan(@RequestBody MealPlanRequest request) {
        MealPlanResponse response = mealPlanService.generate(request);
        return ResponseEntity.ok(response);
    }
}
