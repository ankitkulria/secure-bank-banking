package com.training.controller;


import com.training.dto.*;
import com.training.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

//    register a new user
//    @PostMapping("/register")
//    public ResponseEntity<ApiResponse<AuthResponse>> register(
//            @Valid @RequestBody RegisterRequest request
//            )
//    {
//        AuthResponse authResponse=authService.register(request);
//        return ResponseEntity
//                .status(HttpStatus.CREATED)
//                .body(ApiResponse.success("User registered successfully",authResponse));
//    }

//    Login existing user
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request
    )
    {
        AuthResponse authResponse=authService.login(request);
        return ResponseEntity
                .ok(ApiResponse.success("User logged successfully",authResponse));
    }

    //send otp
    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<String>> sendOtp(
            @RequestBody
            @Valid
            SendOtpRequest request
    )
    {
        authService.sendOtp(request);
        return ResponseEntity.ok(
                ApiResponse.success("OTP sent successfully")
            );
    }

    //resend otp
    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<String>> resendOtp(
            @RequestBody VerifyOtpRequest request
    )
    {
        authService.resendOtp(request.getEmail());

        return ResponseEntity.ok(ApiResponse.success("OTP sent successfully")
        );
    }

    //forgot password
    @PostMapping("/forgot-password/send-otp")
    public ResponseEntity<ApiResponse<String>> sendForgotPasswordOtp(
            @RequestBody
            @Valid
            ForgotPasswordRequest request)
    {
        authService.sendForgotPasswordOtp(request);

        return ResponseEntity.ok(ApiResponse.success("OTP sent successfully"));
    }

    //rest password
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @RequestBody
            @Valid
            ResetPasswordRequest request)
    {
        authService.resetPassword(request);

        return ResponseEntity.ok(
                ApiResponse.success("Password reset successful")
        );
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<String>>
    verifyOtp(
            @RequestBody
            @Valid
            VerifyOtpRequest request
    )
    {
        authService.verifyOtp(request);
        return ResponseEntity.ok(
                ApiResponse.success("Registration successful")
        );
    }
}
