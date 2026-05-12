package com.example.FinalServer.ledger.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntryItemRequest {

    private String name;      // 품목명
    private Integer quantity; // 수량
    private Integer price;    // 금액
    private Integer amount; // 단가 * 수량 (총금액)
}
