package dev.heinisch.menumaestro.exceptions;

import org.springframework.http.HttpStatus;

import java.util.List;

public class ValidationException extends ApplicationException {
    public ValidationException(String message) {
        this(message, List.of());
    }
    public ValidationException(String message, List<String> details) {
        super(message, details, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    public static void fromPropertyChecker(List<String> errors) {
        throw new ValidationException("Validation error occurred!", errors);
    }
}
