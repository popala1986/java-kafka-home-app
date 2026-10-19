package com.homeapp.auth.user;

public class EmailAlreadyTakenException extends RuntimeException {

    public EmailAlreadyTakenException(String email) {
        super("Użytkownik z emailem " + email + " już istnieje");
    }
}
