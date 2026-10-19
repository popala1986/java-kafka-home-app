package com.homeapp.planner.member;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

// Własna kopia kontraktu eventu. Planner NIE importuje klas z user-auth-service.
// Pola, których nie potrzebujemy (email, telefon), po prostu pomijamy.
public record UserRegisteredEvent(
    UUID eventId,
    UUID userId,
    UUID householdId,
    String role,
    String firstName,
    LocalDate birthDate,
    Instant occurredAt
) {
}
