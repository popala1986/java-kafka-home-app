package com.homeapp.planner.digest;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.homeapp.planner.member.HouseholdMemberRepository;

// Automat: codziennie rano wysyła każdemu domownikowi jego zadania na dziś.
@Component
public class DailyDigestScheduler {

    private static final Logger log = LoggerFactory.getLogger(DailyDigestScheduler.class);

    private final DailyDigestService digestService;
    private final HouseholdMemberRepository memberRepository;
    private final Clock clock;

    public DailyDigestScheduler(DailyDigestService digestService, HouseholdMemberRepository memberRepository,
                                Clock clock) {
        this.digestService = digestService;
        this.memberRepository = memberRepository;
        this.clock = clock;
    }

    @Scheduled(cron = "${app.planner.daily-digest-cron}", zone = "Europe/Warsaw")
    public void sendTodayDigests() {
        LocalDate today = LocalDate.now(clock);
        log.info("Automat: wysyłam poranne podsumowania na {}", today);

        for (UUID householdId : memberRepository.findAllHouseholdIds()) {
            try {
                int sent = digestService.sendDigests(householdId, today);
                log.info("Rodzina {}: {} podsumowań", householdId, sent);
            } catch (RuntimeException ex) {
                // Błąd jednej rodziny nie może zatrzymać podsumowań dla pozostałych.
                log.error("Nie udało się wysłać podsumowań dla rodziny {}", householdId, ex);
            }
        }
    }
}
