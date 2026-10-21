package com.homeapp.auth.event;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

// Wysyłany, gdy admin zmieni dane domownika. Niesie dane PO zmianie.
// Email doszedł w Kroku 9b: notification-service buduje z tego eventu swoją listę kontaktów.
public record UserUpdatedEvent(
    UUID eventId,
    UUID userId,
    UUID householdId,
    String firstName,
    String email,
    LocalDate birthDate,
    String phoneNumber,
    Instant occurredAt
) {
}
