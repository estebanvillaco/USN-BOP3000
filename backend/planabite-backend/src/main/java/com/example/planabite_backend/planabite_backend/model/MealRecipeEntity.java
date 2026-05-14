package com.example.planabite_backend.planabite_backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "meal_recipes")
public class MealRecipeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "meal_id", nullable = false)
    private Long mealId;

    @Column(name = "step_number", nullable = false)
    private int stepNumber;

    @Column(name = "instruction", nullable = false)
    private String instruction;

    public Long getId() { return id; }
    public Long getMealId() { return mealId; }
    public int getStepNumber() { return stepNumber; }
    public String getInstruction() { return instruction; }
}
