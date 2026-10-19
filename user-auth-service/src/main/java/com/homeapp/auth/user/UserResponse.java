package com.homeapp.auth.user;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String email,
    String firstName,
    LocalDate birthDate,
    String phoneNumber,
    Role role,
    Instant createdAt
) {

    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getBirthDate(),
            user.getPhoneNumber(),
            user.getRole(),
            user.getCreatedAt());
    }
}
