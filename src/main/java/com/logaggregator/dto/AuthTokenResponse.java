package com.logaggregator.dto;

public class AuthTokenResponse {

    private String token;
    private String tokenType = "Bearer";
    private long expiresInSeconds;

    public AuthTokenResponse() {
    }

    public AuthTokenResponse(String token, long expiresInSeconds) {
        this.token = token;
        this.expiresInSeconds = expiresInSeconds;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public long getExpiresInSeconds() {
        return expiresInSeconds;
    }

    public void setExpiresInSeconds(long expiresInSeconds) {
        this.expiresInSeconds = expiresInSeconds;
    }
}
