package com.example.FinalServer.ledger.entity;

import com.example.FinalServer.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "entries",
        indexes = {
                @Index(name = "idx_entry_date", columnList = "entry_date"),
                @Index(name = "idx_merchant", columnList = "merchant")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Entry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 로그인한 사용자와 연결

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(name = "merchant", length = 120, nullable = false)
    private String merchant;

    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;

    @Column(name = "payment_method", length = 20)
    private String paymentMethod;

    @Column(name = "category", length = 30)
    private String category;

    @Column(name = "currency", length = 3)
    private String currency;

    @Lob
    @Column(name = "raw_text", columnDefinition = "LONGTEXT")
    private String rawText;

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "entry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EntryItem> items;


    /** 저장 전 기본값 보정 */
    @PrePersist
    private void onPrePersist() {
        if (this.entryDate == null) {
            this.entryDate = LocalDate.now();
        }
        if (this.currency == null || this.currency.isBlank()) {
            this.currency = "KRW";
        }
        if (this.totalAmount == null) {
            this.totalAmount = 0L;
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
