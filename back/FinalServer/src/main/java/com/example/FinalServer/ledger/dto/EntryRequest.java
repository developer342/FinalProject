package com.example.FinalServer.ledger.dto;


import com.example.FinalServer.ledger.entity.Entry;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EntryRequest {

    private LocalDate date;
    private String merchant;
    private Long total;
    private String paymentMethod;
    private String category;
    private String currency;
    private String rawText;

    private List<EntryItemRequest> items; // 품목 리스트 추가

    // DTO -> 엔티티 변환
    public Entry toEntity() {
        return Entry.builder()
                .entryDate(date)
                .merchant(merchant)
                .totalAmount(total)
                .paymentMethod(paymentMethod)
                .category(category)
                .currency(currency)
                .rawText(rawText)
                .build();
    }

}
