package com.smlaurindo.realtime_polls.controller;

import com.smlaurindo.realtime_polls.dto.CreatePollRequest;
import com.smlaurindo.realtime_polls.service.PollService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class PollController {

    private final PollService pollService;

    @PostMapping("/polls")
    public ResponseEntity<Map<String, String>> createPoll(
            @RequestBody @Valid CreatePollRequest request
    ) {
        var poll = pollService.createPoll(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(poll.getId())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(Map.of("pollId", poll.getId()));
    }
}
