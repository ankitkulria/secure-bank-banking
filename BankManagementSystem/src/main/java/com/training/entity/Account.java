package com.training.entity;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "accounts")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", nullable = false,unique = true,length = 20)
    private String accountNumber;

    @Column(nullable = false,precision = 15,scale = 2)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type",nullable = false,length = 10)
    private AccountType accountType;

    @Column(name="created_at",nullable = false,updatable = false)
    private LocalDateTime createdAt;

//    prevents concurrent modifications
    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @OneToMany(mappedBy = "sourceAccount",cascade = CascadeType.ALL,orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Transaction> transactions=new ArrayList<>();

    @PrePersist
    protected void onCreate()
    {
        this.createdAt=LocalDateTime.now();
        if(this.balance==null)
        {
            this.balance=BigDecimal.ZERO;
        }
    }
}
