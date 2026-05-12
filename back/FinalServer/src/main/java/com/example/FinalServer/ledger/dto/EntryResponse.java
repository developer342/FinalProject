package com.example.FinalServer.ledger.dto;


import com.example.FinalServer.ledger.entity.Entry;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EntryResponse {

    private Long id;
    private LocalDate date;
    private String merchant;
    private Long total;
    private String paymentMethod;
    private String category;
    private String currency;
    private LocalDateTime createdAt;

    private List<EntryItemResponse> items;

    /**
     * 엔티티 → DTO 변환 (품목 포함)
     */
    public static EntryResponse from(Entry e) {
        List<EntryItemResponse> itemResponses = null;

        if (e.getItems() != null && !e.getItems().isEmpty()) {
            itemResponses = e.getItems().stream()
                    .map(i -> EntryItemResponse.builder()
                            .name(i.getName())
                            .quantity(i.getQuantity())
                            .price(i.getPrice())
                            .amount(i.getAmount())
                            .build())
                    .toList();
        }

        return EntryResponse.builder()
                .id(e.getId())
                .date(e.getEntryDate())
                .merchant(e.getMerchant())
                .total(e.getTotalAmount())
                .paymentMethod(e.getPaymentMethod())
                .category(e.getCategory())
                .currency(e.getCurrency())
                .createdAt(e.getCreatedAt())
                .items(itemResponses)
                .build();
    }
}
