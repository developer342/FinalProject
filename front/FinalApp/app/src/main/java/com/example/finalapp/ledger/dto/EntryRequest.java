package com.example.finalapp.ledger.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntryRequest {
    private String date;           // yyyy-MM-dd
    private String merchant;
    private Long total;
    private String paymentMethod;
    private String category;
    private String currency;
    private String rawText;
}
