package com.homeapp.auth.security;

import java.time.Instant;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.homeapp.auth.user.User;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties properties;

    public JwtService(JwtEncoder jwtEncoder, JwtProperties properties) {
        this.jwtEncoder = jwtEncoder;
        this.properties = properties;
    }

    public String issueToken(User user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("user-auth-service")
            .subject(user.getId().toString())
            .issuedAt(now)
            .expiresAt(now.plus(properties.expiration()))
            .claim("householdId", user.getHousehold().getId().toString())
            .claim("role", user.getRole().name())
            .claim("firstName", user.getFirstName())
            .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public long expiresInSeconds() {
        return properties.expiration().toSeconds();
    }
}
