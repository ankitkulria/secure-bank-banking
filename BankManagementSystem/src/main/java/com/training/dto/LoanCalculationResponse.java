package com.training.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoanCalculationResponse {

    private BigDecimal emi;
    private BigDecimal totalPayment;
    private BigDecimal totalInterest;
}
