package com.homeapp.planner.plan;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.homeapp.planner.chore.Chore;
import com.homeapp.planner.chore.ChoreType;
import com.homeapp.planner.chore.Frequency;
import com.homeapp.planner.member.AgeGroup;
import com.homeapp.planner.member.HouseholdMember;

// Czysta logika układania planu: bez bazy, bez Springa w środku, więc łatwo ją przetestować.
@Component
public class WeeklyPlanner {

    private static final Comparator<HouseholdMember> BY_NAME =
        Comparator.comparing(HouseholdMember::getFirstName).thenComparing(HouseholdMember::getUserId);

    public List<PlannedAssignment> plan(LocalDate weekStart,
                                        List<HouseholdMember> members,
                                        List<Chore> chores,
                                        Map<UUID, Set<UUID>> excludedUsersByChore) {
        // Wiek liczymy raz, na poniedziałek tego tygodnia.
        Map<UUID, Integer> ages = new HashMap<>();
        Map<UUID, Integer> sharedPoints = new HashMap<>();
        for (HouseholdMember member : members) {
            ages.put(member.getUserId(), AgeGroup.ageOn(member.getBirthDate(), weekStart));
            sharedPoints.put(member.getUserId(), 0);
        }

        // Trudniejsze najpierw: wtedy łatwe zadania lepiej wyrównują punkty na końcu.
        List<Chore> activeChores = chores.stream()
            .filter(Chore::isActive)
            .sorted(Comparator.comparing(Chore::getPoints).reversed().thenComparing(Chore::getName))
            .toList();

        List<PlannedAssignment> result = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate day = weekStart.plusDays(i);
            for (Chore chore : activeChores) {
                if (!isScheduledOn(chore.getFrequency(), day)) {
                    continue;
                }
                Set<UUID> excluded = excludedUsersByChore.getOrDefault(chore.getId(), Set.of());
                List<HouseholdMember> eligible = members.stream()
                    .filter(member -> ages.get(member.getUserId()) >= chore.getMinAge())
                    .filter(member -> !excluded.contains(member.getUserId()))
                    .sorted(BY_NAME)
                    .toList();
                if (eligible.isEmpty()) {
                    continue;
                }

                if (chore.getType() == ChoreType.PERSONAL) {
                    // Osobisty: każdy uprawniony robi go dla siebie.
                    for (HouseholdMember member : eligible) {
                        result.add(new PlannedAssignment(day, chore.getId(), member.getUserId()));
                    }
                } else {
                    // Wspólny: dostaje go ta osoba, która ma na razie najmniej punktów za wspólne.
                    HouseholdMember chosen = eligible.stream()
                        .min(Comparator.comparing((HouseholdMember m) -> sharedPoints.get(m.getUserId()))
                            .thenComparing(BY_NAME))
                        .orElseThrow();
                    sharedPoints.merge(chosen.getUserId(), chore.getPoints(), Integer::sum);
                    result.add(new PlannedAssignment(day, chore.getId(), chosen.getUserId()));
                }
            }
        }
        return result;
    }

    // W które dni tygodnia obowiązek się pojawia.
    static boolean isScheduledOn(Frequency frequency, LocalDate day) {
        DayOfWeek dayOfWeek = day.getDayOfWeek();
        return switch (frequency) {
            case DAILY -> true;
            case THREE_TIMES_A_WEEK -> dayOfWeek == DayOfWeek.MONDAY
                || dayOfWeek == DayOfWeek.WEDNESDAY
                || dayOfWeek == DayOfWeek.FRIDAY;
            case WEEKLY -> dayOfWeek == DayOfWeek.SATURDAY;
            // Pierwsza sobota miesiąca.
            case MONTHLY -> dayOfWeek == DayOfWeek.SATURDAY && day.getDayOfMonth() <= 7;
        };
    }
}
