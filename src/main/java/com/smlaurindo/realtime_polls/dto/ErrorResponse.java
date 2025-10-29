package com.smlaurindo.realtime_polls.dto;

import java.time.Instant;

public record ErrorResponse(
        String apiPath,
        int statusCode,
        String title,
        String details,
        Instant timestamp
) {}