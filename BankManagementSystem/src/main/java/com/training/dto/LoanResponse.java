package com.training.dto;

import com.training.entity.LoanStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class LoanResponse {
    private Long loanId;

    private String customerName;

    private String customerEmail;

    private String loanAccountNumber;

    private String repaymentAccountNumber;

    private BigDecimal loanAmount;

    private BigDecimal interestRate;

    private Integer durationMonths;

    private String purpose;

    private LoanStatus status;
}
