package com.example.FinalServer.food.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "allergen",
        uniqueConstraints = { @UniqueConstraint(name = "uk_allergen_name", columnNames = "name") }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Allergen {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;
}
