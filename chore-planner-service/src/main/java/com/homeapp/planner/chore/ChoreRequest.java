package com.homeapp.planner.chore;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// To samo body służy do dodania (POST) i do zmiany (PUT) obowiązku.
public record ChoreRequest(

    @NotBlank(message = "Nazwa jest wymagana")
    @Size(max = 200, message = "Nazwa może mieć najwyżej 200 znaków")
    String name,

    @NotNull(message = "Rodzaj jest wymagany: PERSONAL albo SHARED")
    ChoreType type,

    @NotNull(message = "Częstotliwość jest wymagana")
    Frequency frequency,

    @NotNull(message = "Minimalny wiek jest wymagany")
    @Min(value = 0, message = "Minimalny wiek nie może być ujemny")
    @Max(value = 99, message = "Minimalny wiek może wynosić najwyżej 99")
    Integer minAge,

    @NotNull(message = "Czas trwania jest wymagany")
    @Min(value = 1, message = "Czas trwania to co najmniej 1 minuta")
    @Max(value = 600, message = "Czas trwania to najwyżej 600 minut")
    Integer durationMinutes,

    @NotNull(message = "Punkty są wymagane")
    @Min(value = 1, message = "Punkty: od 1 do 3")
    @Max(value = 3, message = "Punkty: od 1 do 3")
    Integer points,

    @NotNull(message = "Podaj, czy obowiązek jest aktywny (true/false)")
    Boolean active
) {
}
