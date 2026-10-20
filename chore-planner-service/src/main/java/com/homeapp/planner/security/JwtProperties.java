package com.homeapp.planner.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

// Planner tylko SPRAWDZA tokeny, więc potrzebuje samego sekretu (bez czasu ważności).
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret) {
}
