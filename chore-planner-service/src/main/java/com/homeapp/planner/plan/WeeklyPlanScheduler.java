package com.homeapp.planner.plan;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.homeapp.planner.member.HouseholdMemberRepository;

// Automat: w każdą niedzielę wieczorem układa plan na następny tydzień dla każdej rodziny.
@Component
public class WeeklyPlanScheduler {

    private static final Logger log = LoggerFactory.getLogger(WeeklyPlanScheduler.class);

    private final PlanService planService;
    private final HouseholdMemberRepository memberRepository;
    private final Clock clock;

    public WeeklyPlanScheduler(PlanService planService, HouseholdMemberRepository memberRepository, Clock clock) {
        this.planService = planService;
        this.memberRepository = memberRepository;
        this.clock = clock;
    }

    @Scheduled(cron = "${app.planner.weekly-plan-cron}", zone = "Europe/Warsaw")
    public void generateNextWeekPlans() {
        LocalDate nextMonday = LocalDate.now(clock).with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        log.info("Automat: układam plany na tydzień od {}", nextMonday);

        for (UUID householdId : memberRepository.findAllHouseholdIds()) {
            try {
                planService.generatePlanIfMissing(householdId, nextMonday);
            } catch (RuntimeException ex) {
                // Błąd jednej rodziny nie może zatrzymać planów dla pozostałych.
                log.error("Nie udało się ułożyć planu dla rodziny {}", householdId, ex);
            }
        }
    }
}
