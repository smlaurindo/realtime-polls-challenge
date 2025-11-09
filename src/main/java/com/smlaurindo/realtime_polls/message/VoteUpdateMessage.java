package com.smlaurindo.realtime_polls.message;

public record VoteUpdateMessage(
        String id,
        String text,
        int votes
) {}

