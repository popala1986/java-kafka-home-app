package com.homeapp.auth.error;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.homeapp.auth.household.HouseholdNotFoundException;
import com.homeapp.auth.security.InvalidCredentialsException;
import com.homeapp.auth.user.EmailAlreadyTakenException;
import com.homeapp.auth.user.UserNotFoundException;
import com.homeapp.auth.user.CannotDeleteAdminException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
            .forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, "Dane w żądaniu są niepoprawne");
        problem.setTitle("Błąd walidacji");
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler(EmailAlreadyTakenException.class)
    public ProblemDetail handleEmailTaken(EmailAlreadyTakenException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Email zajęty");
        return problem;
    }

    @ExceptionHandler(CannotDeleteAdminException.class)
    public ProblemDetail handleCannotDeleteAdmin(CannotDeleteAdminException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Nie można usunąć");
        return problem;
    }

    @ExceptionHandler(HouseholdNotFoundException.class)
    public ProblemDetail handleHouseholdNotFound(HouseholdNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Nie znaleziono rodziny");
        return problem;
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleUserNotFound(UserNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Nie znaleziono użytkownika");
        return problem;
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail handleInvalidCredentials(InvalidCredentialsException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
        problem.setTitle("Logowanie nieudane");
        return problem;
    }
}
