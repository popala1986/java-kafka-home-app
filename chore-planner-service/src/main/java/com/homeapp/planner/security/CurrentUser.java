package com.homeapp.planner.security;

import java.util.UUID;

import org.springframework.security.oauth2.jwt.Jwt;

// Wygodny odczyt danych zalogowanego użytkownika z tokenu.
public final class CurrentUser {

    private CurrentUser() {
    }

    public static UUID householdId(Jwt jwt) {
        return UUID.fromString(jwt.getClaimAsString("householdId"));
    }
}
