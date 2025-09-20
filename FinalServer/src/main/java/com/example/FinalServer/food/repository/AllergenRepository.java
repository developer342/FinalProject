package com.example.FinalServer.food.repository;

import com.example.FinalServer.food.entity.Allergen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AllergenRepository extends JpaRepository<Allergen, Long> {

  Optional<Allergen> findByName(String name);
  boolean existsByName(String name);
}
