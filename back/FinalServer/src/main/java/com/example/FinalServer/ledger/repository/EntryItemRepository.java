package com.example.FinalServer.ledger.repository;

import com.example.FinalServer.ledger.entity.EntryItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EntryItemRepository extends JpaRepository<EntryItem, Long> {

}
