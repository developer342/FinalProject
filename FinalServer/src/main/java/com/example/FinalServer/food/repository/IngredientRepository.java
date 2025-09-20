package com.example.FinalServer.food.repository;

import com.example.FinalServer.food.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
  Optional<Ingredient> findByName(String name);
  boolean existByName(String name);
}
