package at.codemaestro.exceptions;

import org.springframework.http.HttpStatus;

import java.util.List;

public class ForbiddenException extends ApplicationException{
    public ForbiddenException(String message) {
        this(message, List.of());
    }
    public ForbiddenException(String message, List<String> details) {
        super(message, details, HttpStatus.FORBIDDEN);
    }

    public static ForbiddenException generic() {
        throw new ForbiddenException("Forbidden");
    }
}
