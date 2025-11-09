package com.smlaurindo.realtime_polls.message;

public record WebSocketMessage<T>(
        String type,
        T payload,
        String timestamp
) {}