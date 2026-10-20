package com.homeapp.auth.user;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// PUT podmienia cały profil: wysyłasz wszystkie trzy pola, także te, których nie zmieniasz.
// Brak phoneNumber (albo null) = domownik nie ma telefonu.
public record UpdateMemberRequest(

    @NotBlank(message = "Imię jest wymagane")
    @Size(max = 100)
    String firstName,

    @NotNull(message = "Data urodzenia jest wymagana")
    @Past(message = "Data urodzenia musi być w przeszłości")
    LocalDate birthDate,

    @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "Numer telefonu: 9-15 cyfr, opcjonalnie z + na początku")
    String phoneNumber
) {
}
