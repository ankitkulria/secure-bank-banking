package com.training.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoanCalculationRequest {

    @NotNull(message = "Principal amount is required")
    @Positive(message = "Principal must be positive")
    private BigDecimal principal;

    @NotNull(message = "Annual interest rate is required")
    @Positive(message = "Interest rate must be positive")
    private BigDecimal annualRate;

    @NotNull(message = "Tenure is required")
    @Positive(message = "Tenure must be positive")
    private Integer tenureMonths;

}
