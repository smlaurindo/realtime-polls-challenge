package com.smlaurindo.realtime_polls.dto;

import com.smlaurindo.realtime_polls.validation.annotation.EndDateAfterStartDate;
import jakarta.validation.constraints.*;

import java.time.OffsetDateTime;
import java.util.List;

@EndDateAfterStartDate
public record CreatePollRequest(
        @NotBlank(message = "The question cannot be blank")
        @Size(max = 2000, message = "The question cannot be longer than 2000 characters")
        String question,

        @FutureOrPresent(message = "The poll cannot start in the past")
        OffsetDateTime startsAt,

        @FutureOrPresent(message = "The poll cannot end in the past")
        OffsetDateTime endsAt,

        @Size(min = 3, message = "The poll must have at least 3 options")
        List<@NotBlank(message = "The option cannot be blank") String> options
) {}
