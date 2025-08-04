package com.adithya.trackfolio.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Map;

/**
 * Global exception handler for REST APIs.
 */
@Slf4j
@RestControllerAdvice   // Registers this class as a global exception interceptor for all controllers
public class GlobalExceptionHandler {

    /**
     * Handles ResponseStatusException thrown from any controller or service.
     *
     * @param ex the ResponseStatusException thrown
     * @return ResponseEntity with JSON error details and appropriate HTTP status
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException ex) {
        HttpStatusCode statusCode = ex.getStatusCode();

        String reasonPhrase = "";
        if (statusCode instanceof HttpStatus httpStatus) {
            reasonPhrase = httpStatus.getReasonPhrase();
        }

        Map<String, Object> body = Map.of(
                "timestamp", Instant.now().toString(),
                "status", statusCode.value(),
                "error", reasonPhrase,
                "message", ex.getReason()
        );
        return ResponseEntity.status(statusCode).body(body);
    }
}