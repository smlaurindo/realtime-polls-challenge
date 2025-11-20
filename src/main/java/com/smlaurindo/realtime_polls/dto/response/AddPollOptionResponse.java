package com.smlaurindo.realtime_polls.dto.response;

public record AddPollOptionResponse(
        String id,
        String text,
        Integer votes
) {}
