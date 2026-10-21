package com.homeapp.planner.correlation;

import java.util.UUID;
import java.util.regex.Pattern;

// Correlation ID: jeden identyfikator jednej akcji (np. "Paweł zmienia numer Oli").
// Ten sam ID wędruje przez HTTP, Kafkę i wszystkie serwisy, a każdy serwis wpisuje go do swoich logów.
public final class CorrelationId {

    // Nagłówek HTTP: Postman (albo frontend) może go podać sam, a serwis zawsze go odsyła w odpowiedzi.
    public static final String HTTP_HEADER = "X-Correlation-Id";
    // Nagłówek wiadomości w Kafce.
    public static final String KAFKA_HEADER = "correlationId";
    // Klucz w MDC, czyli w "notesie" logów bieżącego wątku. Wzorzec logów w yaml czyta go jako %X{correlationId}.
    public static final String MDC_KEY = "correlationId";

    // Tylko litery, cyfry i myślniki, maks. 64 znaki. Wszystko inne zastępujemy nowym ID,
    // żeby nikt nie mógł nam wpisać do logów dowolnego tekstu (np. fałszywych linijek).
    private static final Pattern VALID = Pattern.compile("[A-Za-z0-9-]{1,64}");

    private CorrelationId() {
    }

    public static String newId() {
        return UUID.randomUUID().toString();
    }

    public static String orNew(String candidate) {
        return candidate != null && VALID.matcher(candidate).matches() ? candidate : newId();
    }
}
