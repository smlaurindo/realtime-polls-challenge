package com.smlaurindo.realtime_polls.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record EditPollRequest(
        @Size(max = 2000, message = "The question cannot be longer than 2000 characters")
        String question,

        @FutureOrPresent(message = "The poll cannot start in the past")
        OffsetDateTime startsAt,

        @FutureOrPresent(message = "The poll cannot end in the past")
        OffsetDateTime endsAt
) {}
