package com.fares_elsadek.Readly.services;

import com.fares_elsadek.Readly.config.properties.AppProperties;
import com.fares_elsadek.Readly.dtos.ApiResponse;
import com.fares_elsadek.Readly.dtos.LoginRequestDto;
import com.fares_elsadek.Readly.dtos.LoginResponseDto;
import com.fares_elsadek.Readly.exceptions.InvalidTokenException;
import com.fares_elsadek.Readly.repository.TokenRepository;
import com.fares_elsadek.Readly.repository.UserRepository;
import com.fares_elsadek.Readly.entity.Token;
import com.fares_elsadek.Readly.enums.TokenType;
import com.fares_elsadek.Readly.entity.User;
import com.fares_elsadek.Readly.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final AppProperties appProperties;

    public ApiResponse<LoginResponseDto> authenticate(LoginRequestDto request, String clientIp){

        try{
                String email = request.email().toLowerCase().trim();

                // Authenticate the user
                Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                        email,
                        request.password()
                ));

                User user = userRepository.findByEmailIgnoreCase(email)
                        .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

                if(!user.isEnabled())
                    throw new DisabledException("Account not verified. Please check your email for verification link.");

                String accessToken = generateAccessToken(authentication,user.getId());
                String refreshToken = jwtUtil.generateRefreshToken(user.getId());

                LoginResponseDto loginResponse = LoginResponseDto.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .tokenType("Bearer")
                        .expiresIn(3600L) // 1 hour in seconds
                        .user(LoginResponseDto.UserInfo.builder()
                                .id(user.getId())
                                .email(user.getEmail())
                                .roles(user.getRoles().stream()
                                        .map(role -> role.getName().toString())
                                        .collect(Collectors.toList()))
                                .build())
                        .build();

            log.info("User {} authenticated successfully from IP: {}", email, clientIp);
            return ApiResponse.success("Login successful", loginResponse);

        } catch (DisabledException ex) {
            log.warn("Login attempt with unverified account: {}", request.email());
            return ApiResponse.error("Account not verified. Please check your email for verification link.");
        } catch (BadCredentialsException ex) {
            log.warn("Invalid login attempt for email: {} from IP: {}", request.email(), clientIp);
            return ApiResponse.error("Invalid email or password");
        } catch (AuthenticationException ex) {
            log.error("Authentication failed for email: {} from IP: {}", request.email(), clientIp, ex);
            return ApiResponse.error("Authentication failed. Please try again.");
        }
    }

    public ApiResponse<LoginResponseDto> refreshToken(String refreshTokenHeader){

        try{
            var refreshToken = extractTokenFromHeader(refreshTokenHeader);
            if(!StringUtils.hasText(refreshToken))
                return ApiResponse.error("Refresh token is required");

            Claims claims = jwtUtil.validateToken(refreshToken);
            String tokenType = claims.get("type",String.class);
            if(!"refresh".equals(tokenType))
                return ApiResponse.error("Invalid token type");

            var email = claims.getSubject();
            var user = userRepository.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new InvalidTokenException("User not found"));

            if(!user.isEnabled())
                return ApiResponse.error("Account is disabled");

            Authentication authentication = createAuthenticationFromUser(user);
            var newAccessToken = generateAccessToken(authentication, user.getId());
            String newRefreshToken = jwtUtil.generateRefreshToken(email);

            LoginResponseDto response = LoginResponseDto.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .tokenType("Bearer")
                    .expiresIn(3600L)
                    .build();

            log.info("Token refreshed successfully for user: {}", email);
            return ApiResponse.success("Token refreshed successfully", response);

        }catch (InvalidTokenException ex) {
            log.warn("Invalid refresh token: {}", ex.getMessage());
            return ApiResponse.error("Invalid or expired refresh token");
        } catch (Exception ex) {
            log.error("Error refreshing token", ex);
            return ApiResponse.error("Token refresh failed");
        }

    }

    @Transactional
    public ApiResponse<String> verifyEmail(String tokenValue){
        try{
            Token token = tokenRepository.findByTokenAndType(tokenValue, TokenType.EMAIL_VERIFICATION)
                    .orElseThrow(() -> new InvalidTokenException("Invalid verification token"));

            if(token.isConsumed())
                return ApiResponse.error("Verification token has already been used");

            if(token.isExpired())
                return ApiResponse.error("Verification token has expired");

            User user = token.getUser();
            user.setEnabled(true);
            token.setConsumedAt(Instant.now());

            userRepository.save(user);
            tokenRepository.save(token);

            log.info("Email verified successfully for user: {}", user.getEmail());
            return ApiResponse.success("Email verified successfully! You can now log in.", null);

        }catch (InvalidTokenException ex) {
            log.warn("Invalid email verification attempt: {}", ex.getMessage());
            return ApiResponse.error("Invalid or expired verification token");
        }
    }

    public ApiResponse<String> logout(String accessTokenHeader){
        try{
            String token = extractTokenFromHeader(accessTokenHeader);
            if(StringUtils.hasText(token)){
                Claims claims = jwtUtil.validateToken(token);
                String email = claims.getSubject();
                log.info("User {} logged out successfully", email);
            }
        }catch (Exception ex) {
            log.warn("Logout attempt with invalid token", ex);
        }finally {
            return ApiResponse.success("Logged out successfully", null);
        }
    }

    private String generateAccessToken(Authentication authentication,String userId){
        Map<String,Object> claims = new HashMap<>();
        claims.put("authorities",authentication.getAuthorities().stream()
                .map(r -> r.getAuthority())
                .collect(Collectors.toList()));
        claims.put("type","access");
        claims.put("userId",userId);
        return jwtUtil.generateAccessToken(userId,claims);
    }

    private String extractTokenFromHeader(String header){
        if(StringUtils.hasText(header) && header.startsWith("Bearer ")){
            return header.substring(7);
        }
        return null;
    }

    private Authentication createAuthenticationFromUser(User user){
        return new UsernamePasswordAuthenticationToken(user.getEmail(),null,user.getAuthorities());
    }

}
