package com.homeapp.planner.plan;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record ReassignRequest(

    @NotNull(message = "Podaj userId domownika")
    UUID userId
) {
}
