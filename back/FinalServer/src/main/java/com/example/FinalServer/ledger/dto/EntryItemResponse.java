package com.example.FinalServer.ledger.dto;


import com.example.FinalServer.ledger.entity.EntryItem;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntryItemResponse {

    private String name;
    private Integer quantity;
    private Integer price;
    private Integer amount;

    /** EntryItem → DTO 변환 */
    public static EntryItemResponse from(EntryItem item) {
        return EntryItemResponse.builder()
                .name(item.getName())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .amount(item.getAmount())
                .build();
    }
}
