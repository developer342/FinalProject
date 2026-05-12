package com.example.FinalServer.ledger.dto;


import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParseResponse {

    private String date;
    private String merchant;
    private Long total;
    private Double confidence;
    private String paymentMethod;
    private String category;

    private List<EntryItemRequest> items;
}
