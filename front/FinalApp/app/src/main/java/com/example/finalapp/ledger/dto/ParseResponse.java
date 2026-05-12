package com.example.finalapp.ledger.dto;

import java.util.List;

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
public class ParseResponse {
    private String date;           // yyyy-MM-dd
    private String merchant;
    private Long total;
    private Double confidence;
    private String paymentMethod;
    private String category;
    private List<EntryItemRequest> items;
}
