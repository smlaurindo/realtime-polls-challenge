package com.smlaurindo.realtime_polls.controller.advice;

import com.smlaurindo.realtime_polls.dto.error.ErrorResponse;
import com.smlaurindo.realtime_polls.dto.error.ValidationErrorResponse;
import com.smlaurindo.realtime_polls.service.exception.*;
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
public class GlobalExceptionAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            WebRequest request
    ) {
        ValidationErrorResponse errorResponse = new ValidationErrorResponse(
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
                .body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception exception,
            WebRequest request
    ) {
        ErrorResponse errorResponse = new ErrorResponse(
                request.getDescription(false).replace("uri=", ""),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred.",
                Instant.now()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException exception,
            WebRequest request
    ) {
        ErrorResponse errorResponse = new ErrorResponse(
                request.getDescription(false).replace("uri=", ""),
                HttpStatus.NOT_FOUND.value(),
                "Resource Not Found",
                exception.getMessage(),
                Instant.now()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    @ExceptionHandler(PollAlreadyStartedException.class)
    public ResponseEntity<ErrorResponse> handlePollAlreadyStartedException(
            PollAlreadyStartedException exception,
            WebRequest request
    ) {
        ErrorResponse errorResponse =  new ErrorResponse(
                request.getDescription(false).replace("uri=", ""),
                HttpStatus.BAD_REQUEST.value(),
                "Poll Already Started",
                exception.getMessage(),
                Instant.now()
        );


        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    @ExceptionHandler(MinimumPollOptionsException.class)
    public ResponseEntity<ErrorResponse> handleMinimumPollOptionsException(
            MinimumPollOptionsException exception,
            WebRequest request
    ) {
        ErrorResponse errorResponse = new ErrorResponse(
                request.getDescription(false).replace("uri=", ""),
                HttpStatus.BAD_REQUEST.value(),
                "Minimum Poll Options Violation",
                exception.getMessage(),
                Instant.now()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    @ExceptionHandler(PollNotInProgressException.class)
    public ResponseEntity<ErrorResponse> handlePollNotInProgressException(
            PollNotInProgressException exception,
            WebRequest request
    ) {
        ErrorResponse errorResponse = new ErrorResponse(
                request.getDescription(false).replace("uri=", ""),
                HttpStatus.BAD_REQUEST.value(),
                "Poll Not In Progress",
                exception.getMessage(),
                Instant.now()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    @ExceptionHandler(InvalidPollDateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPollDateException(
            InvalidPollDateException exception,
            WebRequest request
    ) {
        ErrorResponse errorResponse = new ErrorResponse(
                request.getDescription(false).replace("uri=", ""),
                HttpStatus.BAD_REQUEST.value(),
                "Invalid Poll Date",
                exception.getMessage(),
                Instant.now()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }
}