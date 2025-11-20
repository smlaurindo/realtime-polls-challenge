package com.smlaurindo.realtime_polls.dto.error;

import java.time.Instant;
import java.util.Map;

public record ValidationErrorResponse(
        String apiPath,
        int statusCode,
        String title,
        String details,
        Map<String, String> errors,
        Instant timestamp
) {}