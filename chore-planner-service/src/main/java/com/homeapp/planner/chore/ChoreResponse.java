package com.homeapp.planner.chore;

import java.util.UUID;

public record ChoreResponse(
    UUID id,
    String code,
    String name,
    ChoreType type,
    Frequency frequency,
    int minAge,
    int durationMinutes,
    int points,
    boolean active
) {

    public static ChoreResponse from(Chore chore) {
        return new ChoreResponse(
            chore.getId(),
            chore.getCode(),
            chore.getName(),
            chore.getType(),
            chore.getFrequency(),
            chore.getMinAge(),
            chore.getDurationMinutes(),
            chore.getPoints(),
            chore.isActive());
    }
}
