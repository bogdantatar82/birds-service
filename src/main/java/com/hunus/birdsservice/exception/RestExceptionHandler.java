package com.hunus.birdsservice.exception;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        LOGGER.info(ex.getMessage());
        return createErrorResponse(ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InternalException.class)
    public ResponseEntity<Object> handleInternalException(InternalException ex) {
        if (ex.getCause() != null) {
            LOGGER.error(String.format("%s \n caused by %s", ex.getMessage(), ex.getCause().getMessage()));
        } else {
            LOGGER.error(ex.getMessage());
        }
        return createErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @SuppressWarnings("unchecked")
    private static <T> ResponseEntity<T> createErrorResponse(Exception ex, HttpStatus status) {
        ErrorResponse errorResponse = new ErrorResponse(status.value(), status.getReasonPhrase(), ex.getMessage(), LocalDateTime.now());
        return new ResponseEntity<>((T) errorResponse, status);
    }
}
