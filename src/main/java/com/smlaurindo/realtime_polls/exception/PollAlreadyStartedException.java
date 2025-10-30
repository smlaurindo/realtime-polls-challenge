package com.smlaurindo.realtime_polls.exception;

public class PollAlreadyStartedException extends RuntimeException {
    public PollAlreadyStartedException(String message) {
        super(message);
    }
}
