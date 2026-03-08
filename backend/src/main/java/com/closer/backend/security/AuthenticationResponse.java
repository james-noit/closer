package com.closer.backend.security;

public class AuthenticationResponse {
    private final String token;
    private final String refreshToken;
    private final String tokenType;
    private final long expiresIn;

    public AuthenticationResponse(String token, String refreshToken, long expiresIn) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.tokenType = "Bearer";
        this.expiresIn = expiresIn;
    }

    public String getToken() {
        return token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }
}