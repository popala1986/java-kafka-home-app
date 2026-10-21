package com.homeapp.notification.notification;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.stream.Collectors;

import com.homeapp.notification.digest.DailyDigestEvent;
import com.homeapp.notification.digest.DigestTask;

// Treść wiadomości. Osobna klasa bez Springa, więc łatwo ją przetestować i poprawiać teksty.
public final class DigestMessages {

    // Jeden SMS to 160 znaków. Dłuższy kosztuje jak dwa albo trzy (to ważne w Kroku 9c).
    static final int SMS_MAX_LENGTH = 160;

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("EEEE d.MM", Locale.of("pl"));

    private DigestMessages() {
    }

    // Np. "Ola, wtorek 20.10: 10 zadań (14 pkt). Pościel łóżko, Nakarm zwierzaka, ..."
    public static String sms(DailyDigestEvent event) {
        String tasks = event.tasks().stream()
            .map(DigestTask::choreName)
            .collect(Collectors.joining(", "));
        String text = "%s, %s: %d zadań (%d pkt). %s".formatted(
            event.firstName(), event.date().format(DAY), event.tasks().size(), event.totalPoints(), tasks);
        return text.length() <= SMS_MAX_LENGTH ? text : text.substring(0, SMS_MAX_LENGTH - 1) + "…";
    }

    public static String emailSubject(DailyDigestEvent event) {
        return "Twoje zadania: %s (%d pkt)".formatted(event.date().format(DAY), event.totalPoints());
    }

    public static String emailBody(DailyDigestEvent event) {
        String tasks = event.tasks().stream()
            .map(task -> "- %s (%d pkt)".formatted(task.choreName(), task.points()))
            .collect(Collectors.joining("\n"));
        return """
            Dzień dobry %s!

            Twoje zadania (%s):
            %s

            Razem: %d pkt. Powodzenia!
            """.formatted(event.firstName(), event.date().format(DAY), tasks, event.totalPoints());
    }
}
