package com.example.FinalServer.food.repository;

import com.example.FinalServer.food.entity.SourceItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SourceItemRepository extends JpaRepository<SourceItem, Long> {

  boolean existsByExternalIdAndHash(String externalId, String hash);
  Optional<SourceItem> findTopByExternalIdOrderByIdDesc(String externalId);
}
