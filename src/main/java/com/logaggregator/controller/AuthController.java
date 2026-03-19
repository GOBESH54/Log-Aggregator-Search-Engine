package com.logaggregator.controller;

import com.logaggregator.config.JwtUtil;
import com.logaggregator.config.SecurityProperties;
import com.logaggregator.dto.AuthLoginRequest;
import com.logaggregator.dto.AuthTokenResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final SecurityProperties securityProperties;
    private final JwtUtil jwtUtil;

    public AuthController(SecurityProperties securityProperties, JwtUtil jwtUtil) {
        this.securityProperties = securityProperties;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthTokenResponse> login(@Valid @RequestBody AuthLoginRequest request) {
        if (!isAuthenticated(request)) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid username or password");
        }

        String token = jwtUtil.generateToken(request.getUsername().trim());
        long expiresInSeconds = securityProperties.getJwt().getExpirationMinutes() * 60;
        return ResponseEntity.ok(new AuthTokenResponse(token, expiresInSeconds));
    }

    private boolean isAuthenticated(AuthLoginRequest request) {
        String configuredUsername = securityProperties.getUser().getUsername();
        String configuredPassword = securityProperties.getUser().getPassword();

        return constantTimeEquals(request.getUsername(), configuredUsername)
                && constantTimeEquals(request.getPassword(), configuredPassword);
    }

    private boolean constantTimeEquals(String left, String right) {
        if (left == null || right == null) {
            return false;
        }

        byte[] leftBytes = left.trim().getBytes(StandardCharsets.UTF_8);
        byte[] rightBytes = right.trim().getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(leftBytes, rightBytes);
    }
}
