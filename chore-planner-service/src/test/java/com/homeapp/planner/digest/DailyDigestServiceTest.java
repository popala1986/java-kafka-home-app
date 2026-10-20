package com.homeapp.planner.digest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.homeapp.planner.chore.ChoreType;
import com.homeapp.planner.plan.AssignmentResponse;
import com.homeapp.planner.plan.PlanService;

class DailyDigestServiceTest {

    private static final UUID HOUSEHOLD_ID = UUID.randomUUID();
    private static final UUID OLA = UUID.randomUUID();
    private static final UUID PAWEL = UUID.randomUUID();
    private static final LocalDate MONDAY = LocalDate.of(2026, 10, 26);
    private static final LocalDate WEDNESDAY = MONDAY.plusDays(2);

    private final PlanService planService = mock(PlanService.class);
    private final DailyDigestPublisher publisher = mock(DailyDigestPublisher.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-28T05:30:00Z"), ZoneId.of("Europe/Warsaw"));
    private final DailyDigestService service = new DailyDigestService(planService, publisher, clock);

    private static AssignmentResponse task(LocalDate date, UUID userId, String firstName, String chore, int points) {
        return new AssignmentResponse(UUID.randomUUID(), date, UUID.randomUUID(), chore,
            ChoreType.PERSONAL, points, userId, firstName, false);
    }

    @Test
    void shouldSendOneDigestPerPersonWithOnlyThatDaysTasks() {
        given(planService.getPlan(HOUSEHOLD_ID, MONDAY)).willReturn(List.of(
            task(MONDAY, OLA, "Ola", "Pościel łóżko", 1),
            task(WEDNESDAY, OLA, "Ola", "Pościel łóżko", 1),
            task(WEDNESDAY, OLA, "Ola", "Nakarm zwierzaka", 2),
            task(WEDNESDAY, PAWEL, "Paweł", "Ugotuj obiad", 5)));

        int sent = service.sendDigests(HOUSEHOLD_ID, WEDNESDAY);

        assertThat(sent).isEqualTo(2);
        ArgumentCaptor<DailyDigestEvent> events = ArgumentCaptor.forClass(DailyDigestEvent.class);
        verify(publisher, times(2)).publish(events.capture());

        DailyDigestEvent ola = events.getAllValues().getFirst();
        assertThat(ola.firstName()).isEqualTo("Ola");
        assertThat(ola.date()).isEqualTo(WEDNESDAY);
        assertThat(ola.tasks()).extracting(DigestTask::choreName)
            .containsExactly("Pościel łóżko", "Nakarm zwierzaka");
        assertThat(ola.totalPoints()).isEqualTo(3);
    }

    @Test
    void shouldSendNothingWhenNoTasksThatDay() {
        given(planService.getPlan(HOUSEHOLD_ID, MONDAY)).willReturn(List.of(
            task(MONDAY, OLA, "Ola", "Pościel łóżko", 1)));

        int sent = service.sendDigests(HOUSEHOLD_ID, WEDNESDAY);

        assertThat(sent).isZero();
        verify(publisher, never()).publish(any());
    }
}
