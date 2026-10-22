package com.homeapp.planner.member;

import java.time.Instant;
import java.util.UUID;

// Własna kopia kontraktu eventu user.deleted (jak UserUpdatedEvent).
public record UserDeletedEvent(
    UUID eventId,
    UUID userId,
    UUID householdId,
    Instant occurredAt
) {
}
