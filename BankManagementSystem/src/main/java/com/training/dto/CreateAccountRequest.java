package com.training.dto;


//DTO for creating new account

import com.training.entity.AccountType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateAccountRequest {

    @NotNull(message = "Account Type is required")
    private AccountType accountType;
}
