package com.homeapp.notification.notification;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.stream.Collectors;

import com.homeapp.notification.digest.DailyDigestEvent;
import com.homeapp.notification.digest.DigestTask;

// Treść wiadomości. Osobna klasa bez Springa, więc łatwo ją przetestować i poprawiać teksty.
public final class DigestMessages {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("EEEE d.MM", Locale.of("pl"));

    private DigestMessages() {
    }

    // Powiadomienie push: krótki tytuł i jedna linijka. Np. "Ola, Twoje zadania na wtorek 20.10"
    public static String pushTitle(DailyDigestEvent event) {
        return "%s, Twoje zadania na %s".formatted(event.firstName(), event.date().format(DAY));
    }

    // Np. "Zadania: 2, razem 3 pkt. Pościel łóżko, Nakarm zwierzaka". Za długi tekst telefon sam utnie.
    public static String pushBody(DailyDigestEvent event) {
        String tasks = event.tasks().stream()
            .map(DigestTask::choreName)
            .collect(Collectors.joining(", "));
        return "Zadania: %d, razem %d pkt. %s".formatted(event.tasks().size(), event.totalPoints(), tasks);
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
