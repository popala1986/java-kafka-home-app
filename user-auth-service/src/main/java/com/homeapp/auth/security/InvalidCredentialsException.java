package com.homeapp.auth.security;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Niepoprawny email lub hasło");
    }
}
