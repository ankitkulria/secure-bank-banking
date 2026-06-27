package com.training.service;

import com.training.dto.UserResponse;
import com.training.entity.User;
import com.training.exception.ResourceNotFoundException;
import com.training.repository.UserRepository;
import com.training.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

//    Load user by email for spring security authenticationn

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user=userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: "+email));
        return new CustomUserDetails(user);
    }

//    Get all users-- admin only
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

//    Get users by id
    public UserResponse getUserById(Long id) {
        User user=userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User","id",id));
        return mapToResponse(user);
    }

    //        map user-- response DTO
    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
