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
public class EntryResponse {
    private Long id;
    private String date;
    private String merchant;
    private Long total;
    private String paymentMethod;
    private String category;
    private String currency;
    private String createdAt;
    private List<EntryItemResponse> items;
}
