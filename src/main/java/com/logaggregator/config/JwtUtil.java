package com.logaggregator.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecurityProperties securityProperties;
    private SecretKey secretKey;

    public JwtUtil(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @PostConstruct
    public void init() {
        String secret = securityProperties.getJwt().getSecret();
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT secret must be configured via JWT_SECRET environment variable");
        }
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 characters long");
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String username) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(securityProperties.getJwt().getExpirationMinutes() * 60);

        return Jwts.builder()
                .subject(username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(secretKey)
                .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration() != null && claims.getExpiration().after(new Date());
        } catch (Exception ex) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
