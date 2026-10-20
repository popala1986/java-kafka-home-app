package com.homeapp.planner.member;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

// Własna kopia kontraktu eventu user.updated. Telefonu planner nie potrzebuje, więc go pomijamy.
public record UserUpdatedEvent(
    UUID eventId,
    UUID userId,
    UUID householdId,
    String firstName,
    LocalDate birthDate,
    Instant occurredAt
) {
}
