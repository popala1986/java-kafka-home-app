package com.homeapp.planner.plan;

import java.time.LocalDate;
import java.util.UUID;

import com.homeapp.planner.chore.ChoreType;

public record AssignmentResponse(
    UUID id,
    LocalDate date,
    UUID choreId,
    String choreName,
    ChoreType choreType,
    int points,
    UUID userId,
    String firstName,
    boolean manual
) {
}
