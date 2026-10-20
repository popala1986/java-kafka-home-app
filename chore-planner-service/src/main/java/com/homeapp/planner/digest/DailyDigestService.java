package com.homeapp.planner.digest;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.homeapp.planner.plan.AssignmentResponse;
import com.homeapp.planner.plan.PlanService;

@Service
public class DailyDigestService {

    private final PlanService planService;
    private final DailyDigestPublisher publisher;
    private final Clock clock;

    public DailyDigestService(PlanService planService, DailyDigestPublisher publisher, Clock clock) {
        this.planService = planService;
        this.publisher = publisher;
        this.clock = clock;
    }

    // Wysyła poranne podsumowanie każdemu domownikowi, który ma tego dnia jakieś zadanie.
    // Zwraca, ile osób dostało podsumowanie.
    public int sendDigests(UUID householdId, LocalDate date) {
        LocalDate weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // LinkedHashMap trzyma kolejność z planu (posortowanego po imieniu).
        Map<UUID, List<AssignmentResponse>> tasksByUser = planService.getPlan(householdId, weekStart).stream()
            .filter(assignment -> assignment.date().equals(date))
            .collect(Collectors.groupingBy(AssignmentResponse::userId, LinkedHashMap::new, Collectors.toList()));

        Instant now = Instant.now(clock);
        tasksByUser.forEach((userId, assignments) -> {
            List<DigestTask> tasks = assignments.stream()
                .map(assignment -> new DigestTask(assignment.choreName(), assignment.points()))
                .toList();
            int totalPoints = tasks.stream().mapToInt(DigestTask::points).sum();

            publisher.publish(new DailyDigestEvent(
                UUID.randomUUID(),
                userId,
                householdId,
                assignments.getFirst().firstName(),
                date,
                tasks,
                totalPoints,
                now));
        });
        return tasksByUser.size();
    }
}
