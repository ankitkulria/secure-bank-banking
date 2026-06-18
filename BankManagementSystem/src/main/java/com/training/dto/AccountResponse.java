package com.training.dto;


//DTO for account detail response

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountResponse {

    private Long id;
    private String accountHolderName;
    private String accountNumber;
    private BigDecimal balance;
    private String accountType;
    private LocalDateTime createdAt;
}
