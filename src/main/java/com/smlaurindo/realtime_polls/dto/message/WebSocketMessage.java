package com.smlaurindo.realtime_polls.dto.message;

public record WebSocketMessage<T>(
        String type,
        T payload,
        String timestamp
) {}