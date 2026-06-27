package com.training.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="loans")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //loan owner
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    //loan account
    @ManyToOne
    @JoinColumn(name = "loan_account_id")
    private Account loanAccount;

    //repayment account
    @ManyToOne
    @JoinColumn(name = "repayment_account_id")
    private Account repaymentAccount;

    // Loan Details
    @Column(nullable = false)
    private BigDecimal loanAmount;

    @Column(nullable = false)
    private BigDecimal remainingAmount;

    @Column(nullable = false)
    private BigDecimal interestRate;

    @Column(nullable = false)
    private BigDecimal monthlyEmi;

    @Column(nullable = false)
    private Integer durationMonths;

    @Column(length = 500)
    private String purpose;

    // Dates
    private LocalDate nextDueDate;

    private LocalDateTime appliedAt;

    private LocalDateTime approvedAt;

    // Status
    @Enumerated(EnumType.STRING)
    private LoanStatus status;
}
