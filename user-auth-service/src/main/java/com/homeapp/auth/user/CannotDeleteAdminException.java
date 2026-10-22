package com.homeapp.auth.user;

// Rodzica (konto ADMIN) nie usuwamy: rodzina zostałaby bez nikogo, kto nią zarządza.
public class CannotDeleteAdminException extends RuntimeException {

    public CannotDeleteAdminException() {
        super("Nie można usunąć rodzica (konta ADMIN)");
    }
}
