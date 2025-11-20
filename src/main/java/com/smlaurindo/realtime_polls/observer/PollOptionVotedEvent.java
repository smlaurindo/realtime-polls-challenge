package com.smlaurindo.realtime_polls.observer;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;

@Getter
public class PollOptionVotedEvent extends ApplicationEvent {

    private final String pollId;
    private final String optionId;
    private final Instant eventTimestamp;

    public PollOptionVotedEvent(Object source, String pollId, String optionId, Instant eventTimestamp) {
        super(source);
        this.pollId = pollId;
        this.optionId = optionId;
        this.eventTimestamp = eventTimestamp;
    }
}
