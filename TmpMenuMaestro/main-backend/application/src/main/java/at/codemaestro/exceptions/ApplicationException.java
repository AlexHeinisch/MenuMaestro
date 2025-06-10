package at.codemaestro.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
@AllArgsConstructor
public class ApplicationException extends RuntimeException {
    private final String message;
    private final List<String> details;
    private final HttpStatus status;
}
