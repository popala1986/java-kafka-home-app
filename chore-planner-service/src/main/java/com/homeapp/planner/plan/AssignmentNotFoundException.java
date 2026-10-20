package com.homeapp.planner.plan;

import java.util.UUID;

public class AssignmentNotFoundException extends RuntimeException {

    public AssignmentNotFoundException(UUID assignmentId) {
        super("Zadanie o id " + assignmentId + " nie istnieje");
    }
}
