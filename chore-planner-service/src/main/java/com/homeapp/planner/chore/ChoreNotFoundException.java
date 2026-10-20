package com.homeapp.planner.chore;

import java.util.UUID;

public class ChoreNotFoundException extends RuntimeException {

    public ChoreNotFoundException(UUID choreId) {
        super("Obowiązek o id " + choreId + " nie istnieje");
    }
}
