package com.training.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanApplicationRequest {

    @NotBlank
    private String loanAccountNumber;

    @NotNull
    @DecimalMin("100000")
    private BigDecimal loanAmount;

    @NotNull
    @Min(6)
    @Max(360)
    private Integer durationMonths;

    @NotBlank
    private String purpose;

    @NotBlank
    private String repaymentAccountNumber;
}
