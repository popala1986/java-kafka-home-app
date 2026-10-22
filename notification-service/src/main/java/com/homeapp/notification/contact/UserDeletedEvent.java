package com.homeapp.notification.contact;

import java.time.Instant;
import java.util.UUID;

// Kopia kontraktu user.deleted.
public record UserDeletedEvent(
    UUID eventId,
    UUID userId,
    UUID householdId,
    Instant occurredAt
) {
}
