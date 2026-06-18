package com.training.controller;


import com.training.dto.AccountResponse;
import com.training.dto.ApiResponse;
import com.training.dto.CreateAccountRequest;
import com.training.entity.Account;
import com.training.security.CustomUserDetails;
import com.training.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateAccountRequest request
            )
    {
        AccountResponse account=accountService.createAccount(userDetails.getUser().getId(),request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Account Created Successfully",account));
    }

    /**
     * Get all accounts of the authenticated user.
     * GET /api/accounts
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getMyAccounts(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<AccountResponse> accounts = accountService.getAccountsByUserId(
                userDetails.getUser().getId());

        return ResponseEntity.ok(ApiResponse.success(accounts));
    }

    /**
     * Get a specific account by ID.
     * GET /api/accounts/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountById(
            @PathVariable Long id) {

        AccountResponse account = accountService.getAccountById(id);
        return ResponseEntity.ok(ApiResponse.success(account));
    }

// account by account number
    @GetMapping("/account-number/{accountNumber}")
    public ResponseEntity<ApiResponse<AccountResponse>>
    getByAccountNumber(
            @PathVariable String accountNumber
    )
    {
        return ResponseEntity.ok(
                ApiResponse.success(
                        accountService.getByAccountNumber(accountNumber)
                )
        );
    }
}
