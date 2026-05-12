package com.example.finalapp.ledger.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EntryItemRequest {

    private String name;
    private Integer quantity;
    private Integer price;
    private Integer amount;
}
