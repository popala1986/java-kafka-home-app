package com.homeapp.notification.digest;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

// Kopia kontraktu chore.daily.digest z plannera.
public record DailyDigestEvent(
    UUID eventId,
    UUID userId,
    UUID householdId,
    String firstName,
    LocalDate date,
    List<DigestTask> tasks,
    int totalPoints,
    Instant occurredAt
) {
}
