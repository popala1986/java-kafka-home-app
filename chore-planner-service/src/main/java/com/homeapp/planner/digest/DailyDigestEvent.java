package com.homeapp.planner.digest;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

// "Dzień dobry Ola, dziś masz 12 zadań (18 pkt)". Jeden event = jedna osoba na jeden dzień.
// Kontaktu (telefon, email) tu nie ma: zna go notification-service.
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
