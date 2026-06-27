package com.training.controller;


import com.training.dto.AccountResponse;
import com.training.dto.ApiResponse;
import com.training.dto.LoanResponse;
import com.training.dto.UserResponse;
import com.training.service.AccountService;
import com.training.service.LoanService;
import com.training.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/admin")
public class AdminController {

    private final UserService userService;
    private final AccountService accountService;
    private final LoanService loanService;

//    Get all registered users
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users= userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users));
    }

//    Get a specific user by ID
    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

//    Get all account of a specific user (ADmmin)
    @GetMapping("/users/{id}/accounts")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAccountsByUser(
            @PathVariable Long id
    )
    {
        List<AccountResponse> accounts=accountService.getAccountsByUserId(id);
        return ResponseEntity.ok(ApiResponse.success(accounts));
    }

    @GetMapping("/loans/pending")
    public ResponseEntity<ApiResponse<List<LoanResponse>>> getPendingLoans()
    {
        return ResponseEntity.ok(ApiResponse.success(loanService.getPendingLoans()));
    }

    //get all accounts
    @GetMapping("/accounts")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAllAccounts()
    {
        return ResponseEntity.ok(
                ApiResponse.success(
                        accountService.getAllAccounts()
                )
        );
    }

    @PostMapping("/loans/{loanId}/approve")
    public ResponseEntity<ApiResponse<String>> approveLoan(
            @PathVariable
            Long loanId)
    {
        loanService.approveLoan(loanId);

        return ResponseEntity.ok(ApiResponse.success("Loan approved successfully"));
    }

    @PostMapping("/loans/{loanId}/reject")
    public ResponseEntity<ApiResponse<String>>
    rejectLoan(@PathVariable Long loanId)
    {
        loanService.rejectLoan(loanId);

        return ResponseEntity.ok(ApiResponse.success("Loan rejected successfully"));
    }

    @GetMapping("/loans")
    public ResponseEntity<ApiResponse<List<LoanResponse>>> getAllLoans()
    {
        return ResponseEntity.ok(
                ApiResponse.success(loanService.getAllLoans()));
    }


    //get reserve account
    @GetMapping("/reserve-account")
    public ResponseEntity<ApiResponse<AccountResponse>>
    getReserveAccount()
    {
        return ResponseEntity.ok(
                ApiResponse.success(
                        accountService.getReserveAccount()
                )
        );
    }
}
