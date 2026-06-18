package com.training.service;


import com.training.Util.OtpUtil;
import com.training.dto.*;
import com.training.entity.OtpPurpose;
import com.training.entity.PendingUser;
import com.training.entity.Role;
import com.training.entity.User;
import com.training.exception.BadRequestException;
import com.training.exception.DuplicateResourceException;
import com.training.repository.PendingUserRepository;
import com.training.repository.UserRepository;
import com.training.security.JwtTokenProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    private final PendingUserRepository pendingUserRepository;
    private final EmailService emailService;
    private final OtpUtil otpUtil;

//    register new user- check for email(duplicate), password hashing,default role-user
//    @Transactional
//    public AuthResponse register(RegisterRequest request)
//    {
////        check if email already exists
//        if(userRepository.existsByEmail(request.getEmail()))
//        {
//            throw new DuplicateResourceException("User","Email",request.getEmail());
//        }
////        Build and save user entity
//        User user=User.builder()
//                .name(request.getName())
//                .email(request.getEmail())
//                .password(passwordEncoder.encode(request.getPassword()))
//                .role(Role.USER)
//                .build();
//        userRepository.save(user);
//        log.info("User {} has been registered",user.getEmail());

//        Generate JWT token for immediate login after registration
//        String token=jwtTokenProvider.generateTokenFromEmail(user.getEmail());
//
//        return AuthResponse.builder()
//                .token(token)
//                .name(user.getName())
//                .email(user.getEmail())
//                .role(user.getRole().name())
//                .build();
//    }

//    Authenticate user and return JWT token
    public AuthResponse login(LoginRequest request)
    {
        Authentication authentication=authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

//        Get JWT from authenticated principal
        String token=jwtTokenProvider.generateToken(authentication);
        log.info("user logged in succesfully:{}",request.getEmail());

//        Get role from the authenticated user
        User user=userRepository.findByEmail(request.getEmail()).orElseThrow();

        return AuthResponse.builder()
                .token(token)
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    //OTP
    public void sendOtp(
            SendOtpRequest request)
    {
        // Existing User Check
        if(userRepository.existsByEmail(request.getEmail()))
        {
            throw new DuplicateResourceException("User","email",request.getEmail());
        }
        // Delete old pending user
        pendingUserRepository
                .findByEmail(request.getEmail())
                .ifPresent(pendingUserRepository::delete);
        // Generate OTP
        String otp = otpUtil.generateOtp();
        PendingUser pendingUser =
                PendingUser.builder()
                        .name(request.getName())
                        .email(request.getEmail())
                        .password(passwordEncoder.encode(request.getPassword()))
                        .otp(otp)
                        .otpExpiry(LocalDateTime.now().plusMinutes(5))
                        .build();
        pendingUserRepository.save(
                pendingUser
        );
        // Send Email
        emailService.sendEmail(
                request.getEmail(),
                "OTP Verification",
                "Your OTP is: "
                        + otp
                        + "\nValid for 5 minutes."
        );
    }

    //resend otp
    public void resendOtp(String email)
    {
        PendingUser pendingUser =
                pendingUserRepository
                        .findByEmail(email)
                        .orElseThrow(() -> new BadRequestException("Registration not found"));
        String otp =
                otpUtil.generateOtp();

        pendingUser.setOtp(otp);
        pendingUser.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        pendingUserRepository.save(
                pendingUser
        );

        emailService.sendEmail(
                email,
                "OTP Verification",
                "Your OTP is: "
                        + otp
                        + "\nValid for 5 minutes."
        );
    }
//otp verify
    public void verifyOtp(VerifyOtpRequest request)
    {
        PendingUser pendingUser = pendingUserRepository.findByEmail(request.getEmail())
                        .orElseThrow(() -> new BadRequestException("OTP not found"));

        // Expiry Check
        if(LocalDateTime.now().isAfter(pendingUser.getOtpExpiry()))
        {
            throw new BadRequestException("OTP expired");
        }

        // OTP Match
        if(!pendingUser.getOtp().equals(request.getOtp()))
        {
            throw new BadRequestException("Invalid OTP");
        }

        // Create User
        User user = User.builder()
                        .name(pendingUser.getName())
                        .email(pendingUser.getEmail())
                        .password(pendingUser.getPassword())
                        .role(Role.USER)
                        .build();

        userRepository.save(user);
        //delete from pending user
        pendingUserRepository.delete(pendingUser);
    }
    //forgot pasword
    public void sendForgotPasswordOtp(ForgotPasswordRequest request)
    {
        User user = userRepository
                        .findByEmail(request.getEmail())
                        .orElseThrow(() -> new BadRequestException("Email not registered"));

        pendingUserRepository
                .findByEmailAndPurpose(request.getEmail(), OtpPurpose.PASSWORD_RESET)
                .ifPresent(pendingUserRepository::delete);

        String otp = otpUtil.generateOtp();

        PendingUser pendingUser = PendingUser.builder()
                        .email(user.getEmail())
                        .otp(otp)
                        .purpose(OtpPurpose.PASSWORD_RESET)
                        .otpExpiry(LocalDateTime.now().plusMinutes(5))
                        .build();

        pendingUserRepository.save(pendingUser);

        emailService.sendEmail(
                user.getEmail(),
                "Password Reset OTP", "Your OTP is: " + otp + "\nValid for 5 minutes.");
    }

    //reset password
    public void resetPassword(ResetPasswordRequest request)
    {
        PendingUser pendingUser =
                pendingUserRepository.findByEmailAndPurpose(
                                request.getEmail(),
                                OtpPurpose.PASSWORD_RESET)
                        .orElseThrow(() -> new BadRequestException("OTP not found"));

        if(LocalDateTime.now().isAfter(pendingUser.getOtpExpiry()))
        {
            throw new BadRequestException("OTP expired");
        }

        if(!pendingUser.getOtp().equals(request.getOtp()))
        {
            throw new BadRequestException("Invalid OTP");
        }

        User user = userRepository
                        .findByEmail(request.getEmail())
                        .orElseThrow(() -> new BadRequestException("User not found"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        userRepository.save(user);
        pendingUserRepository.delete(pendingUser);
    }
}
