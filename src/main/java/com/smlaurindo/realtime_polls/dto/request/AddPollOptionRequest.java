package com.smlaurindo.realtime_polls.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AddPollOptionRequest(
        @NotBlank(message = "The option cannot be blank")
        String text
) {}
