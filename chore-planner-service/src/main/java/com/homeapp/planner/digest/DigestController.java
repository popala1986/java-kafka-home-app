package com.homeapp.planner.digest;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.homeapp.planner.security.CurrentUser;

// Ręczne "wyślij podsumowania teraz" dla swojej rodziny. Przydaje się do testów i gdy automat coś pominął.
@RestController
@RequestMapping("/api/planner/digests")
public class DigestController {

    private final DailyDigestService digestService;

    public DigestController(DailyDigestService digestService) {
        this.digestService = digestService;
    }

    @PostMapping("/{date}")
    public DigestResponse sendDigests(@AuthenticationPrincipal Jwt jwt,
                                      @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        int sent = digestService.sendDigests(CurrentUser.householdId(jwt), date);
        return new DigestResponse(date, sent);
    }
}
