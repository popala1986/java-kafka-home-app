package com.homeapp.notification.notification;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
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
    void shouldWriteShortSmsWithDayCountAndPoints() {
        DailyDigestEvent event = digest(List.of(new DigestTask("Pościel łóżko", 1), new DigestTask("Nakarm zwierzaka", 2)));

        assertThat(DigestMessages.sms(event))
            .isEqualTo("Ola, wtorek 20.10: 2 zadań (3 pkt). Pościel łóżko, Nakarm zwierzaka");
        assertThat(DigestMessages.emailBody(event))
            .contains("Dzień dobry Ola!")
            .contains("- Nakarm zwierzaka (2 pkt)")
            .contains("Razem: 3 pkt.");
    }

    @Test
    void shouldCutLongSmsTo160Characters() {
        DailyDigestEvent event = digest(Collections.nCopies(20, new DigestTask("Odłóż zabawki na miejsce", 1)));

        String sms = DigestMessages.sms(event);

        assertThat(sms).hasSize(DigestMessages.SMS_MAX_LENGTH).endsWith("…");
    }
}
