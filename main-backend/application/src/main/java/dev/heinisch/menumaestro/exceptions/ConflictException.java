package dev.heinisch.menumaestro.exceptions;

import org.springframework.http.HttpStatus;

import java.util.List;

public class ConflictException extends ApplicationException {
    public ConflictException(String message) {
        this(message, List.of());
    }
    public ConflictException(String message, List<String> details) {
        super(message, details, HttpStatus.CONFLICT);
    }
}
