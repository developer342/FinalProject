package com.example.FinalServer.food.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
  name = "search_queue",
  indexes = {
          @Index(name = "idx_search_queue_status_created", columnList = "status, createdAt"),
          @Index(name = "idx_search_queue_query", columnList = "query")
  }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SearchQueue {

  public enum Type { PRODUCT, INGREDIENT }
  public enum Status { PENDING, DONE, FAILED }

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String query;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private Type type;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private Status status;

  @Builder.Default
  @Column(nullable = false)
  private int tries = 0;

  private Instant lastTriedAt;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @PrePersist
  void onCreate() { this.createdAt = Instant.now(); }
}
