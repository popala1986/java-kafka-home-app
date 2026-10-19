package com.homeapp.auth.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.LocalDate;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import com.homeapp.auth.household.Household;
import com.homeapp.auth.user.Role;
import com.homeapp.auth.user.User;

class JwtServiceTest {

    private final SecurityConfig config = new SecurityConfig();
    private final JwtProperties properties =
        new JwtProperties("test-secret-test-secret-test-secret-32", Duration.ofHours(1));
    private final SecretKey key = config.jwtSecretKey(properties);
    private final JwtService jwtService = new JwtService(config.jwtEncoder(key), properties);
    private final JwtDecoder decoder = config.jwtDecoder(key);

    @Test
    void tokenShouldCarryUserHouseholdAndRole() {
        Household household = new Household("Opalowie");
        User user = new User(household, "pawel@example.com", "hash", "Paweł",
            LocalDate.of(1986, 3, 15), "+48600100200", Role.ADMIN);

        String token = jwtService.issueToken(user);
        Jwt jwt = decoder.decode(token);

        assertThat(jwt.getSubject()).isEqualTo(user.getId().toString());
        assertThat(jwt.getClaimAsString("householdId")).isEqualTo(household.getId().toString());
        assertThat(jwt.getClaimAsString("role")).isEqualTo("ADMIN");
        assertThat(jwt.getExpiresAt()).isAfter(jwt.getIssuedAt());
    }
}
