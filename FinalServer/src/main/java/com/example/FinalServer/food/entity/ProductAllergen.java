package com.example.FinalServer.food.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "product_allergen",
        uniqueConstraints = { @UniqueConstraint(name = "uk_product_allergen", columnNames = {"product_id","allergen_id"}) }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ProductAllergen {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "allergen_id", nullable = false)
  private Allergen allergen;
}
