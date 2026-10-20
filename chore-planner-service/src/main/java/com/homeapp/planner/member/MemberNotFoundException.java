package com.homeapp.planner.member;

import java.util.UUID;

public class MemberNotFoundException extends RuntimeException {

    public MemberNotFoundException(UUID userId) {
        super("Domownik o id " + userId + " nie należy do Twojej rodziny");
    }
}
