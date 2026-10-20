package com.homeapp.auth.event;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

// Wysyłany, gdy admin zmieni dane domownika. Niesie dane PO zmianie.
public record UserUpdatedEvent(
    UUID eventId,
    UUID userId,
    UUID householdId,
    String firstName,
    LocalDate birthDate,
    String phoneNumber,
    Instant occurredAt
) {
}
