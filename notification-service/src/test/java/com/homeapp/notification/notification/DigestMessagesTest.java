package com.homeapp.notification.notification;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.homeapp.notification.digest.DailyDigestEvent;
import com.homeapp.notification.digest.DigestTask;

class DigestMessagesTest {

    private static DailyDigestEvent digest(List<DigestTask> tasks) {
        int points = tasks.stream().mapToInt(DigestTask::points).sum();
        return new DailyDigestEvent(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "Ola",
            LocalDate.of(2026, 10, 20), tasks, points, Instant.now());
    }

    @Test
    void shouldWritePushWithDayCountAndPoints() {
        DailyDigestEvent event = digest(List.of(new DigestTask("Pościel łóżko", 1), new DigestTask("Nakarm zwierzaka", 2)));

        assertThat(DigestMessages.pushTitle(event)).isEqualTo("Ola, Twoje zadania na wtorek 20.10");
        assertThat(DigestMessages.pushBody(event))
            .isEqualTo("Zadania: 2, razem 3 pkt. Pościel łóżko, Nakarm zwierzaka");
    }

    @Test
    void shouldWriteEmailWithEveryTask() {
        DailyDigestEvent event = digest(List.of(new DigestTask("Pościel łóżko", 1), new DigestTask("Nakarm zwierzaka", 2)));

        assertThat(DigestMessages.emailSubject(event)).isEqualTo("Twoje zadania: wtorek 20.10 (3 pkt)");
        assertThat(DigestMessages.emailBody(event))
            .contains("Dzień dobry Ola!")
            .contains("- Nakarm zwierzaka (2 pkt)")
            .contains("Razem: 3 pkt.");
    }
}
