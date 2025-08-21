package com.fares_elsadek.Readly.filters;

import com.fares_elsadek.Readly.exceptions.InvalidTokenException;
import com.fares_elsadek.Readly.utils.CustomUserPrincipal;
import com.fares_elsadek.Readly.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtValidatorFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = extractTokenFromHeader(request);
        if(StringUtils.hasText(token)){
            try{
                Claims claims = jwtUtil.validateToken(token);

                // Verify token type
                String tokenType = claims.get("type",String.class);
                if(!"access".equals(tokenType)){
                    sendErrorResponse(response,"Invalid token type", HttpStatus.UNAUTHORIZED);
                    return;
                }

                Authentication authentication = createAuthentication(claims);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("JWT token validated successfully for user: {}", claims.getSubject());

            }catch (InvalidTokenException ex){
                log.warn("Invalid JWT token: {}", ex.getMessage());
                sendErrorResponse(response, ex.getMessage(), HttpStatus.UNAUTHORIZED);
                return;
            }catch (Exception ex){
                log.error("Unexpected error during token validation", ex);
                sendErrorResponse(response, "Token validation failed", HttpStatus.UNAUTHORIZED);
                return;
            }
        }

        filterChain.doFilter(request,response);
    }

    private String extractTokenFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private Authentication createAuthentication(Claims claims) {
        String subject = claims.getSubject();
        String userId  = claims.get("userId",String.class);
        List<String> authorities = claims.get("authorities", List.class);

        List<SimpleGrantedAuthority> grantedAuthorities = authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();

        var principal= new CustomUserPrincipal(subject,userId);
        return new UsernamePasswordAuthenticationToken(principal, null, grantedAuthorities);
    }

    private void sendErrorResponse(HttpServletResponse response, String message, HttpStatus status)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> errorBody = Map.of(
                "error", "Authentication Failed",
                "message", message,
                "status", status.value()
        );

        response.getWriter().write(objectMapper.writeValueAsString(errorBody));
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.startsWith("/api/v1/auth/") ||
                path.startsWith("/swagger-ui/") ||
                path.startsWith("/v3/api-docs");
    }
}
