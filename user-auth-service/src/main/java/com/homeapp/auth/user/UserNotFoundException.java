package com.homeapp.auth.user;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(UUID userId) {
        super("Użytkownik o id " + userId + " nie istnieje");
    }
}
