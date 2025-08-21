package com.fares_elsadek.Readly.controller;

import com.fares_elsadek.Readly.dtos.ApiResponse;
import com.fares_elsadek.Readly.dtos.LoginRequestDto;
import com.fares_elsadek.Readly.dtos.LoginResponseDto;
import com.fares_elsadek.Readly.dtos.RegisterRequestDto;
import com.fares_elsadek.Readly.services.AuthenticationService;
import com.fares_elsadek.Readly.services.RegisterService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Slf4j
@Tag(name = "Authentication Controller")
public class AuthController {
    private final RegisterService registerService;
    private final AuthenticationService authenticationService;
    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody @Valid RegisterRequestDto requestDto){
        log.info("Registration request received for email: {}", requestDto.email());
        return ResponseEntity.ok(registerService.register(requestDto));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(
            @Valid @RequestBody LoginRequestDto request,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIpAddress(httpRequest);
        log.info("Login request received for email: {} from IP: {}", request.email(), clientIp);

        ApiResponse<LoginResponseDto> response = authenticationService.authenticate(request, clientIp);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<LoginResponseDto>> refreshToken(
            @RequestHeader("Authorization") String refreshToken) {

        log.info("Token refresh request received");
        ApiResponse<LoginResponseDto> response = authenticationService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<String>> verifyEmail(@RequestParam String token) {
        log.info("Email verification request received");
        ApiResponse<String> response = authenticationService.verifyEmail(token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
            @RequestHeader("Authorization") String accessToken) {

        log.info("Logout request received");
        ApiResponse<String> response = authenticationService.logout(accessToken);
        return ResponseEntity.ok(response);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}
