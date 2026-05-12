package com.example.FinalServer.ledger.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entry_id", nullable = false)
    private Entry entry;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false)
    private Integer amount;
}
