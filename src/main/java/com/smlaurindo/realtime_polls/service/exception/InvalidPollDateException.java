package com.smlaurindo.realtime_polls.service.exception;

public class InvalidPollDateException extends RuntimeException {
    public InvalidPollDateException(String message) {
        super(message);
    }
}
