package com.fares_elsadek.Readly.utils;

import com.fares_elsadek.Readly.config.properties.JwtProperties;
import com.fares_elsadek.Readly.exceptions.InvalidTokenException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Component
@Slf4j
public class JwtUtil {
    private final SecretKey key;
    private final JwtProperties jwtProperties;
    public JwtUtil(JwtProperties jwtProperties){
        this.jwtProperties = jwtProperties;
        byte[] secretBytes = java.util.Base64.getDecoder().decode(jwtProperties.secret());
        this.key = Keys.hmacShaKeyFor(secretBytes);
    }

    public String generateAccessToken(String subject, Map<String, Object> claims){
        return generateToken(subject, claims, jwtProperties.accessTokenExpiration());
    }

    public String generateRefreshToken(String subject) {
        return generateToken(subject, Map.of("type", "refresh"), jwtProperties.refreshTokenExpiration());
    }

    public String generateToken(String subject, Map<String,Object> claims,Long expiration){
        return Jwts.builder()
                .header()
                .type("JWT")
                .and()
                .issuer("Readly")
                .claims(claims)
                .subject(subject)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusMillis(expiration)))
                .signWith(this.key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims validateToken(String token){
        try{
            return Jwts.parser()
                    .verifyWith(this.key)
                    .build()
                    .parseSignedClaims(token).getPayload();
        }catch (SignatureException ex) {
            log.warn("Invalid JWT signature: {}", ex.getMessage());
            throw new InvalidTokenException("Invalid token signature");
        } catch (MalformedJwtException ex) {
            log.warn("Malformed JWT token: {}", ex.getMessage());
            throw new InvalidTokenException("Malformed token");
        } catch (ExpiredJwtException ex) {
            log.warn("Expired JWT token: {}", ex.getMessage());
            throw new InvalidTokenException("Token expired");
        } catch (UnsupportedJwtException ex) {
            log.warn("Unsupported JWT token: {}", ex.getMessage());
            throw new InvalidTokenException("Unsupported token");
        } catch (IllegalArgumentException ex) {
            log.warn("JWT token compact is empty: {}", ex.getMessage());
            throw new InvalidTokenException("Empty token");
        }
    }

    public boolean isTokenExpired(String token){
        try{
            return validateToken(token).getExpiration().before(new Date());
        }catch (InvalidTokenException ex){
            return true;
        }
    }

    public String extractSubject(String token){
        return validateToken(token).getSubject();
    }
}
