package com.smlaurindo.realtime_polls.exception;

public class InvalidPollDateException extends RuntimeException {
    public InvalidPollDateException(String message) {
        super(message);
    }
}
