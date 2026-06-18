package com.training.controller;

import com.training.dto.*;
import com.training.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;

    //apply
    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<String>> applyLoan(
            @Valid @RequestBody LoanApplicationRequest request, Authentication authentication)
    {
        loanService.applyLoan(request,authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Loan Application submitted successfully"));
    }

//    Calculate EMI for a laon
    @PostMapping("/calculate-emi")
    public ResponseEntity<ApiResponse<LoanCalculationResponse>> calculateEmi
    (@Valid @RequestBody LoanCalculationRequest request) {
        LoanCalculationResponse result=loanService.calculateEmi(request);

        return ResponseEntity.ok(ApiResponse.success("EMI calculated successfully:",result));
    }

    //get loan --user
    @GetMapping("/my-loans")
    public ResponseEntity<
            ApiResponse<List<LoanDetailsResponse>>> getMyLoans(Authentication authentication)
    {
        return ResponseEntity.ok(ApiResponse.success(loanService.getMyLoans(authentication.getName())));
    }

    //pay emi-- user
    @PostMapping("/pay-emi")
    public ResponseEntity<ApiResponse<String>> payEmi(Authentication authentication)
    {
        loanService.payEmi(authentication.getName());

        return ResponseEntity.ok(ApiResponse.success("EMI paid successfully"));
    }
}
