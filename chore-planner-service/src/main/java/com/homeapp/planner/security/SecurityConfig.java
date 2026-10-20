package com.homeapp.planner.security;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

// Planner nie wydaje tokenów (to robi user-auth-service), tylko je sprawdza.
// Dlatego jest tu JwtDecoder, a nie ma JwtEncodera.
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/actuator/health/**", "/actuator/info").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/planner/chores").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/planner/chores/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/planner/chores/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/planner/chores/*/exclusions/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/planner/chores/*/exclusions/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/planner/plans/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/planner/plans/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/planner/plans/*/assignments/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/planner/digests/*").hasRole("ADMIN")
                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder(JwtProperties properties) {
        SecretKey key = new SecretKeySpec(properties.secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    }

    // Tak samo jak w user-auth-service: claim "role": "ADMIN" zamieniamy na ROLE_ADMIN.
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("role");
        authorities.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }
}
