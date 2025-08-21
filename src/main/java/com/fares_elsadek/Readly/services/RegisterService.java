package com.fares_elsadek.Readly.services;

import com.fares_elsadek.Readly.config.properties.EmailProperties;
import com.fares_elsadek.Readly.dtos.ApiResponse;
import com.fares_elsadek.Readly.dtos.RegisterRequestDto;
import com.fares_elsadek.Readly.entity.Role;
import com.fares_elsadek.Readly.entity.Token;
import com.fares_elsadek.Readly.entity.User;
import com.fares_elsadek.Readly.exceptions.RoleNotFoundException;
import com.fares_elsadek.Readly.exceptions.UserAlreadyExistsException;
import com.fares_elsadek.Readly.repository.RoleRepository;
import com.fares_elsadek.Readly.repository.TokenRepository;
import com.fares_elsadek.Readly.repository.UserRepository;
import com.fares_elsadek.Readly.enums.*;
import com.fares_elsadek.Readly.utils.SecureTokenGenerator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Validated
@Slf4j
public class RegisterService {
    private final EmailService emailService;
    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureTokenGenerator secureTokenGenerator;
    private final RoleRepository roleRepository;
    private final EmailProperties emailProperties;

    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<String> register(@Valid RegisterRequestDto requestDto){

        String email = requestDto.email().toLowerCase().trim();
        log.info("Attempting to register user with email: {}", email);

        // Check if user already exists
        if (userRepository.existsByEmailIgnoreCase(email)) {
            log.warn("Registration attempt with existing email: {}", email);
            throw new UserAlreadyExistsException("An account with this email already exists");
        }


        // Create user
        User user = createUser(email, requestDto.password());

        // Create verification token
        Token verificationToken = createVerificationToken(user);

        // Save entities
        User savedUser = userRepository.save(user);
        tokenRepository.save(verificationToken);

        // Send verification email asynchronously
        emailService.sendVerificationEmailAsync(email, verificationToken.getToken());

        log.info("User registered successfully with ID: {}", savedUser.getId());

        ApiResponse<String> response = ApiResponse.success(
                "Registration successful! Please check your email to verify your account.",
                "User registered with ID: " + savedUser.getId());

        return response;
    }

    private User createUser(String email, String password) {
        Role userRole = getUserRole();

        return User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .roles(List.of(userRole))
                .enabled(false) // User must verify email first
                .accountLocked(true)
                .build();
    }

    private Role getUserRole(){
        return roleRepository.findByName(RoleType.USER)
                .orElseThrow(() -> new RoleNotFoundException("USER role not found in system"));
    }

    private Token createVerificationToken(User user) {
        Instant expirationTime = Instant.now()
                .plusSeconds(emailProperties.verificationTokenTtlMinutes() * 60L);

        return Token.builder()
                .token(secureTokenGenerator.newToken())
                .type(TokenType.EMAIL_VERIFICATION)
                .expiresAt(expirationTime)
                .user(user)
                .build();
    }
}
