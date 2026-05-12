package com.example.FinalServer.ledger.repository;

import com.example.FinalServer.ledger.entity.Entry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface EntryRepository extends JpaRepository<Entry, Long> {

    // 기간으로 조회(페이징 지원). CSV 내보내기 시에는 Pageable.unpaged()로 호출.
    Page<Entry> findByEntryDateBetween(LocalDate from, LocalDate to, Pageable pageable);

    /** 날짜+상호+총액 동일한 최근 1건(중복 방지) */
    Optional<Entry> findTop1ByEntryDateAndMerchantAndTotalAmountOrderByCreatedAtDesc(
            LocalDate entryDate, String merchant, Long totalAmount
    );
}
