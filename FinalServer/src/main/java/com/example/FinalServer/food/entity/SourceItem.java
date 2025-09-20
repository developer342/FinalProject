package com.example.FinalServer.food.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
  name = "source_item",
  indexes = { @Index(name = "idx_source_item_external_id", columnList = "externalId") },
  uniqueConstraints = { @UniqueConstraint(name = "uk_source_item_ext_hash", columnNames = {"externalId","hash"}) }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SourceItem {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 32)
  private String source;

  @Column(nullable = false, length = 64)
  private String externalId;

  @Lob
  @Column(columnDefinition = "LONGTEXT")
  private String rawJson;

  @Column(nullable = false, length = 128)
  private String hash;

  private Instant fetchedAt;
}
