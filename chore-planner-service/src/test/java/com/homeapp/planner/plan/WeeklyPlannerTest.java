package com.homeapp.planner.plan;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.homeapp.planner.chore.Chore;
import com.homeapp.planner.chore.ChoreRequest;
import com.homeapp.planner.chore.ChoreType;
import com.homeapp.planner.chore.Frequency;
import com.homeapp.planner.member.HouseholdMember;

class WeeklyPlannerTest {

    // Poniedziałek 2 listopada 2026. Sobota tego tygodnia (7.11) to pierwsza sobota miesiąca.
    private static final LocalDate MONDAY = LocalDate.of(2026, 11, 2);
    private static final UUID HOUSEHOLD_ID = UUID.randomUUID();

    private final WeeklyPlanner planner = new WeeklyPlanner();

    private final HouseholdMember mama = member("Anna", 1985);
    private final HouseholdMember ola = member("Ola", 2014);    // 12 lat
    private final HouseholdMember kuba = member("Kuba", 2015);  // 11 lat
    private final HouseholdMember zosia = member("Zosia", 2024); // 2 lata

    private static HouseholdMember member(String name, int birthYear) {
        return new HouseholdMember(UUID.randomUUID(), HOUSEHOLD_ID, name,
            LocalDate.of(birthYear, 1, 1), "USER", Instant.now());
    }

    private static Chore chore(String name, ChoreType type, Frequency frequency, int minAge, int points, boolean active) {
        return new Chore(HOUSEHOLD_ID,
            new ChoreRequest(name, type, frequency, minAge, 10, points, active), Instant.now());
    }

    private static long countFor(List<PlannedAssignment> plan, HouseholdMember member) {
        return plan.stream().filter(a -> a.userId().equals(member.getUserId())).count();
    }

    @Test
    void personalChoreGoesToEveryoneOldEnoughEveryDay() {
        Chore bed = chore("Pościel łóżko", ChoreType.PERSONAL, Frequency.DAILY, 4, 1, true);

        List<PlannedAssignment> plan = planner.plan(MONDAY, List.of(mama, ola, zosia), List.of(bed), Map.of());

        assertThat(countFor(plan, mama)).isEqualTo(7);
        assertThat(countFor(plan, ola)).isEqualTo(7);
        assertThat(countFor(plan, zosia)).isZero(); // za mała
    }

    @Test
    void sharedChoreGoesToOnePersonPerDayAndIsBalanced() {
        Chore trash = chore("Wynieś śmieci", ChoreType.SHARED, Frequency.DAILY, 11, 1, true);

        List<PlannedAssignment> plan = planner.plan(MONDAY, List.of(ola, kuba), List.of(trash), Map.of());

        assertThat(plan).hasSize(7); // jedno zadanie dziennie, nie dwa
        assertThat(Math.abs(countFor(plan, ola) - countFor(plan, kuba))).isLessThanOrEqualTo(1);
    }

    @Test
    void excludedMemberNeverGetsTheChore() {
        Chore dog = chore("Wyprowadź psa", ChoreType.SHARED, Frequency.DAILY, 11, 2, true);

        List<PlannedAssignment> plan = planner.plan(MONDAY, List.of(ola, kuba), List.of(dog),
            Map.of(dog.getId(), Set.of(ola.getUserId())));

        assertThat(countFor(plan, ola)).isZero();
        assertThat(countFor(plan, kuba)).isEqualTo(7);
    }

    @Test
    void inactiveChoreIsSkipped() {
        Chore off = chore("Nakarm kota", ChoreType.SHARED, Frequency.DAILY, 4, 1, false);

        assertThat(planner.plan(MONDAY, List.of(mama, ola), List.of(off), Map.of())).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "DAILY, 7",
        "THREE_TIMES_A_WEEK, 3",
        "WEEKLY, 1",
        "MONTHLY, 1"
    })
    void frequencyDecidesHowManyDaysInWeek(Frequency frequency, int expectedDays) {
        Chore plants = chore("Podlej kwiaty", ChoreType.SHARED, frequency, 18, 1, true);

        assertThat(planner.plan(MONDAY, List.of(mama), List.of(plants), Map.of())).hasSize(expectedDays);
    }

    @Test
    void monthlyChoreSkipsWeekWithoutFirstSaturday() {
        Chore windows = chore("Umyj okna", ChoreType.SHARED, Frequency.MONTHLY, 18, 3, true);

        // Tydzień od 9.11: sobota 14.11 to już nie pierwsza sobota miesiąca.
        assertThat(planner.plan(MONDAY.plusWeeks(1), List.of(mama), List.of(windows), Map.of())).isEmpty();
    }
}
