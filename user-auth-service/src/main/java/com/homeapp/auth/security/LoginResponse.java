package com.homeapp.auth.security;

public record LoginResponse(String accessToken, String tokenType, long expiresIn) {
}
