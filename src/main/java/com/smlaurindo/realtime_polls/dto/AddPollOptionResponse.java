package com.smlaurindo.realtime_polls.dto;

public record AddPollOptionResponse(
        String id,
        String text,
        Integer votes
) {}
