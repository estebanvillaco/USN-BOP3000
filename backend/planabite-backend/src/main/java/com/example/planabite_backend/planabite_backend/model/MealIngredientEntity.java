package com.example.planabite_backend.planabite_backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "meal_ingredients")
public class MealIngredientEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_id", nullable = false)
    private MealEntity meal;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "amount")
    private String amount;

    @Column(name = "search_term")
    private String searchTerm;

    @Column(name = "min_price")
    private double minPrice;

    public Long getId() { return id; }
    public MealEntity getMeal() { return meal; }
    public String getDisplayName() { return displayName; }
    public String getAmount() { return amount; }
    public String getSearchTerm() { return searchTerm; }
    public double getMinPrice() { return minPrice; }
}
