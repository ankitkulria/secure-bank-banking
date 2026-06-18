package com.training.dto;

import com.training.entity.LoanStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class LoanDetailsResponse {

    private Long loanId;

    private BigDecimal loanAmount;

    private BigDecimal remainingAmount;

    private BigDecimal monthlyEmi;

    private BigDecimal interestRate;

    private LocalDate nextDueDate;

    private LoanStatus status;

    private Integer durationMonths;

    private String purpose;
}
