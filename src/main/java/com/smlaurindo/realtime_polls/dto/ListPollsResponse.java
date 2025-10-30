package com.smlaurindo.realtime_polls.dto;

import com.smlaurindo.realtime_polls.domain.PollStatus;

import java.util.List;

public record ListPollsResponse(
        String id,
        String question,
        PollStatus status,
        String startsAt,
        String endsAt,
        List<OptionResponse> options
) {
    public record OptionResponse(
            String id,
            String text,
            int votes
    ) {}
}
