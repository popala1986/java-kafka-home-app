package com.homeapp.auth.household;

import java.util.UUID;

public class HouseholdNotFoundException extends RuntimeException {

    public HouseholdNotFoundException(UUID householdId) {
        super("Rodzina o id " + householdId + " nie istnieje");
    }
}
