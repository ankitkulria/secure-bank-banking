package com.training.controller;

import com.training.dto.*;
import com.training.security.CustomUserDetails;
import com.training.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    //Deposit money
    // api/transactions/deposit
    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<TransactionResponse>> deposit(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody DepositRequest request
            )
    {
        TransactionResponse transaction=transactionService.deposit(
                userDetails.getUser().getId(), request
        );

        return ResponseEntity.ok(ApiResponse.success("Deposit Successful",transaction));
    }

    //withdraw
    //api/transactions/withdraw
    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<TransactionResponse>> withdraw(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody WithdrawRequest request) {

        TransactionResponse transaction = transactionService.withdraw(
                userDetails.getUser().getId(), request);

        return ResponseEntity.ok(ApiResponse.success("Withdrawal successful", transaction));
    }

    //transfer
    //api/transactions/transfer
    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransactionResponse>> transfer(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TransferRequest request) {

        TransactionResponse transaction = transactionService.transfer(
                userDetails.getUser().getId(), request);

        return ResponseEntity.ok(ApiResponse.success("Transfer successful", transaction));
    }

    //paginated history
    //api/transactions/history/{accountId}?page=0&size=10
    @GetMapping("/history/{accountId}")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getTransactionHistory(
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("timeStamp").descending());
        Page<TransactionResponse> history = transactionService.getTransactionHistory(accountId, pageable);

        return ResponseEntity.ok(ApiResponse.success(history));
    }
}
