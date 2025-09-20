package com.example.FinalServer.food.repository;

import com.example.FinalServer.food.entity.ProductIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductIngredientRepository extends JpaRepository<ProductIngredient, Long> {

}
