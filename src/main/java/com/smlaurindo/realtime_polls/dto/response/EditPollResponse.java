package com.smlaurindo.realtime_polls.dto.response;

import com.smlaurindo.realtime_polls.domain.PollStatus;

import java.util.List;

public record EditPollResponse(
        String id,
        String question,
        PollStatus status,
        String startsAt,
        String endsAt,
        List<EditPollResponse.OptionResponse> options
) {
    public record OptionResponse(
            String id,
            String text,
            int votes
    ) {}
}
