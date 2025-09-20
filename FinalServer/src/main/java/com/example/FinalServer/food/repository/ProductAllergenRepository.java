package com.example.FinalServer.food.repository;

import com.example.FinalServer.food.entity.ProductAllergen;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductAllergenRepository extends JpaRepository<ProductAllergen, Long> {
}
