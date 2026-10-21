package com.homeapp.notification.contact;

import java.time.Instant;
import java.util.UUID;

// Kopia kontraktu user.updated. Data urodzenia nas tu nie interesuje, więc jej nie ma.
public record UserUpdatedEvent(
    UUID eventId,
    UUID userId,
    UUID householdId,
    String firstName,
    String email,
    String phoneNumber,
    Instant occurredAt
) {
}
