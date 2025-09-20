package com.example.FinalServer.food.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "ingredient",
        uniqueConstraints = { @UniqueConstraint(name = "uk_ingredient_name", columnNames = "name") }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Ingredient {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false) // 정규화된 성분명(소문자/트림 등)
  private String name;
}
