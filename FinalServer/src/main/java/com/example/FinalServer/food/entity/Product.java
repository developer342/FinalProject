package com.example.FinalServer.food.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;


@Entity
@Table(
        name = "product",
        indexes = { @Index(name = "idx_product_name", columnList = "name") },
        uniqueConstraints = { @UniqueConstraint(name = "uk_product_report_no", columnNames = "prdlstReportNo") }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Product {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 64)
  private String prdlstReportNo; // 품목보고번호(고유)

  @Column(nullable = false)
  private String name;

  private String manufacturer;
  private String seller;
  private String kind;
  private String capacity;
  private String barcode;
  private String imgUrl;

  @CreationTimestamp
  @Column(updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  private Instant updatedAt;
}
