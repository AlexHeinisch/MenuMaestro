package at.codemaestro.exceptions;

import org.springframework.http.HttpStatus;

import java.util.List;

public class NotFoundException extends ApplicationException {
    public NotFoundException(String message) {
        this(message, List.of());
    }
    public NotFoundException(String message, List<String> details) {
        super(message, details, HttpStatus.NOT_FOUND);
    }

    public static NotFoundException forEntityAndId(String entityName, Long id) {
        return new NotFoundException(String.format("%s with id '%d' not found", entityName, id));
    }
}
