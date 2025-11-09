package com.smlaurindo.realtime_polls.exception;

public class PollNotInProgressException extends RuntimeException {
    public PollNotInProgressException(String message) {
        super(message);
    }
}
