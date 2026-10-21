package com.homeapp.notification.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

// Tak jak planner: tylko SPRAWDZAMY tokeny wydane przez user-auth-service.
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret) {
}
