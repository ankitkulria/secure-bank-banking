package com.training.entity;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 15)
    private TransactionType type;

    @Column(nullable = false,precision = 15,scale = 2)
    private BigDecimal amount;

    @Column(name = "balance_after",nullable = false,precision = 15,scale = 2)
    private BigDecimal balanceAfter;

    @Column(length = 255)
    private String description;

    @Column(nullable = false,updatable = false)
    private LocalDateTime timeStamp;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_account_id",nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Account sourceAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_account_id",nullable = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Account targetAccount;

    @PrePersist
    protected void onCreate()
    {
        this.timeStamp=LocalDateTime.now();
    }
}
