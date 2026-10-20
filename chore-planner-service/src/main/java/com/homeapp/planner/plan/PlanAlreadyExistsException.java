package com.homeapp.planner.plan;

import java.time.LocalDate;

public class PlanAlreadyExistsException extends RuntimeException {

    public PlanAlreadyExistsException(LocalDate weekStart) {
        super("Plan na tydzień od " + weekStart + " już istnieje. Usuń go, jeśli chcesz ułożyć od nowa.");
    }
}
