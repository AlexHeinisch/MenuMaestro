package dev.heinisch.menumaestro.endpoint;

import dev.heinisch.menumaestro.exceptions.ApplicationException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.ErrorResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionMapper {

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponse> handleApplicationExceptions(ApplicationException ex) {
        return ResponseEntity
                .status(ex.getStatus())
                .body(
                        new ErrorResponse()
                                .status(ex.getStatus().value())
                                .message(ex.getMessage())
                                .details(ex.getDetails())
                );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message("Bad request")
                        .details(List.of(
                                        "This may indicate a missing required parameter, such as request body",
                                        "Exception message: " + ex.getMessage()
                                )
                        ));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleGenericExceptions(RuntimeException ex) {
        log.error("An error occurred", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        new ErrorResponse()
                                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .message("Internal Server Error occurred")
                                .details(List.of())
                );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String s = ex.getRequiredType() != null
                ? ex.getRequiredType().getSimpleName()
                : "<no type>";
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        new ErrorResponse()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .message("Parameter '" + ex.getName() + "' is not valid for type '" + s + "'")
                                .details(List.of())
                );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(
                        new ErrorResponse()
                                .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                                .message(ex.getMessage())
                                .details(List.of())
                );
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationDenied(AuthorizationDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(
                        new ErrorResponse()
                                .status(HttpStatus.FORBIDDEN.value())
                                .message(ex.getMessage())
                                .details(List.of())
                );
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Access-Control-Allow-Origin", "*"); // Add CORS headers as needed
        headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH, OPTIONS");
        headers.add("Access-Control-Allow-Headers", "*");
        headers.add("Access-Control-Expose-Headers", "*");
        return ResponseEntity
                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                .headers(headers)
                .body(
                        new ErrorResponse()
                                .status(HttpStatus.PAYLOAD_TOO_LARGE.value())
                                .message(ex.getMessage())
                                .details(List.of())
                );
    }
}
