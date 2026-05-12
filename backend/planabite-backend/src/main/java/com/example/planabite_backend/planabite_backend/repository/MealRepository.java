package com.example.planabite_backend.planabite_backend.repository;

import com.example.planabite_backend.planabite_backend.model.MealEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealRepository extends JpaRepository<MealEntity, Long> {
}
