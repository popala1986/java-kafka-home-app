package com.homeapp.auth.user;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(

    @NotBlank(message = "Nazwa rodziny jest wymagana")
    @Size(max = 100, message = "Nazwa rodziny może mieć maksymalnie 100 znaków")
    String householdName,

    @NotBlank(message = "Email jest wymagany")
    @Email(message = "Niepoprawny format emaila")
    @Size(max = 255)
    String email,

    @NotBlank(message = "Hasło jest wymagane")
    @Size(min = 8, max = 72, message = "Hasło musi mieć od 8 do 72 znaków")
    String password,

    @NotBlank(message = "Imię jest wymagane")
    @Size(max = 100)
    String firstName,

    @NotNull(message = "Data urodzenia jest wymagana")
    @Past(message = "Data urodzenia musi być w przeszłości")
    LocalDate birthDate,

    @NotBlank(message = "Numer telefonu jest wymagany")
    @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "Numer telefonu: 9-15 cyfr, opcjonalnie z + na początku")
    String phoneNumber
) {
}
