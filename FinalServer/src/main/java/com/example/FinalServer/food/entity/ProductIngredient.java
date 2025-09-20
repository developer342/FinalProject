package com.example.FinalServer.food.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "product_ingredient",
        uniqueConstraints = { @UniqueConstraint(name = "uk_product_ingredient", columnNames = {"product_id","ingredient_id"}) }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ProductIngredient {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id; // PK

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product; // 제품

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "ingredient_id", nullable = false)
  private Ingredient ingredient; // 성분
}
