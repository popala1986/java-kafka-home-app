package com.homeapp.auth.security;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

// Wartości z application.yaml (sekcja app.jwt) jako obiekt Javy.
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, Duration expiration) {
}
