package com.smlaurindo.realtime_polls.dto.message;

public record VoteUpdateMessage(
        String id,
        String text,
        int votes
) {}

