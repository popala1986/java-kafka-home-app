package com.homeapp.planner.plan;

import java.time.LocalDate;
import java.util.UUID;

// Wynik algorytmu, zanim trafi do bazy: kto, co i kiedy.
public record PlannedAssignment(LocalDate date, UUID choreId, UUID userId) {
}
