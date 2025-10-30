package com.smlaurindo.realtime_polls.exception;

import com.smlaurindo.realtime_polls.dto.ErrorResponse;
import com.smlaurindo.realtime_polls.dto.ValidationErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.web.context.request.WebRequest;

import java.time.Instant;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            WebRequest request
    ) {
        ValidationErrorResponse body = new ValidationErrorResponse(
                request.getDescription(false).replace("uri=", ""),
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Validation Failed",
                "Request validation failed",
                exception.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage)),
                Instant.now()
        );

        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception exception,
            WebRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        request.getDescription(false).replace("uri=", ""),
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Internal Server Error",
                        "An unexpected error occurred.",
                        Instant.now()
                ));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException exception,
            WebRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                        request.getDescription(false).replace("uri=", ""),
                        HttpStatus.NOT_FOUND.value(),
                        "Resource Not Found",
                        exception.getMessage(),
                        Instant.now()
                ));
    }

    @ExceptionHandler(PollAlreadyStartedException.class)
    public ResponseEntity<ErrorResponse> handlePollAlreadyStartedException(
            PollAlreadyStartedException exception,
            WebRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(
                        request.getDescription(false).replace("uri=", ""),
                        HttpStatus.CONFLICT.value(),
                        "Poll Already Started",
                        exception.getMessage(),
                        Instant.now()
                ));
    }

    @ExceptionHandler(InvalidPollDateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPollDateException(
            InvalidPollDateException exception,
            WebRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        request.getDescription(false).replace("uri=", ""),
                        HttpStatus.BAD_REQUEST.value(),
                        "Invalid Poll Date",
                        exception.getMessage(),
                        Instant.now()
                ));
    }
}