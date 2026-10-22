package com.homeapp.auth.event;

import java.time.Instant;
import java.util.UUID;

// Wysyłany, gdy admin usunie domownika. Wystarczy samo id: inne serwisy kasują swoją kopię tej osoby.
public record UserDeletedEvent(
    UUID eventId,
    UUID userId,
    UUID householdId,
    Instant occurredAt
) {
}
