package com.example.planabite_backend.planabite_backend.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "meals")
public class MealEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "search_term")
    private String searchTerm;

    @Column(name = "goal")
    private String goal;

    @Column(name = "diet_type")
    private String dietType;

    @Column(name = "tags")
    private String tags;

    @Column(name = "default_servings")
    private int defaultServings;

    @OneToMany(mappedBy = "meal", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<MealIngredientEntity> ingredients;

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getSearchTerm() { return searchTerm; }
    public String getGoal() { return goal; }
    public String getDietType() { return dietType; }
    public String getTags() { return tags; }
    public int getDefaultServings() { return defaultServings; }
    public List<MealIngredientEntity> getIngredients() { return ingredients; }
}
