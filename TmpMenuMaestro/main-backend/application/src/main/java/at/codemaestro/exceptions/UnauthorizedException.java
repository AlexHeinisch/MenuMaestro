package at.codemaestro.exceptions;

import org.springframework.http.HttpStatus;

import java.util.List;

public class UnauthorizedException extends ApplicationException {
    public UnauthorizedException(String message) {
        this(message, List.of());
    }
    public UnauthorizedException(String message, List<String> details) {
        super(message, details, HttpStatus.UNAUTHORIZED);
    }

    public static UnauthorizedException generic() {
        throw new UnauthorizedException("Unauthorized");
    }
}
