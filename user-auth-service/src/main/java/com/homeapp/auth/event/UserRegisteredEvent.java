package com.homeapp.auth.event;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record UserRegisteredEvent(
    UUID eventId,
    UUID userId,
    UUID householdId,
    String role,
    String email,
    String firstName,
    LocalDate birthDate,
    String phoneNumber,
    Instant occurredAt
) {
}
