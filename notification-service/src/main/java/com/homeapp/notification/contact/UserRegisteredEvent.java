package com.homeapp.notification.contact;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

// Własna kopia kontraktu z user-auth-service. Tym razem potrzebujemy emaila i telefonu.
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
