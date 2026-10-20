package com.homeapp.planner.plan;

import java.time.LocalDate;

public class InvalidWeekStartException extends RuntimeException {

    public InvalidWeekStartException(LocalDate weekStart) {
        super("Tydzień musi zaczynać się w poniedziałek, a " + weekStart + " to " + weekStart.getDayOfWeek());
    }
}
