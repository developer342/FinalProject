package com.example.FinalServer.food.repository;

import com.example.FinalServer.food.entity.SearchQueue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SourceQueueRepository extends JpaRepository<SearchQueue, Long> {

  List<SearchQueue> findTop100ByStatusOrderByCreatedAtAsc(SearchQueue.Status status);
}
