package com.homeapp.auth.user;

import java.time.LocalDate;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddMemberRequest(

    @NotBlank(message = "Imię jest wymagane")
    @Size(max = 100)
    String firstName,

    @NotNull(message = "Data urodzenia jest wymagana")
    @Past(message = "Data urodzenia musi być w przeszłości")
    LocalDate birthDate,

    @Email(message = "Niepoprawny format emaila")
    @Size(max = 255)
    String email,

    @Size(min = 8, max = 72, message = "Hasło musi mieć od 8 do 72 znaków")
    String password,

    @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "Numer telefonu: 9-15 cyfr, opcjonalnie z + na początku")
    String phoneNumber
) {

    @AssertTrue(message = "Podaj email i hasło razem albo żadne z nich")
    public boolean isLoginDataComplete() {
        return (email == null) == (password == null);
    }
}
