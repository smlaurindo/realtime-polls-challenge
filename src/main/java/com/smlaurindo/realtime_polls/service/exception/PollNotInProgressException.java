package com.smlaurindo.realtime_polls.service.exception;

public class PollNotInProgressException extends RuntimeException {
    public PollNotInProgressException(String message) {
        super(message);
    }
}
