package com.smlaurindo.realtime_polls.controller;

import com.smlaurindo.realtime_polls.domain.PollStatus;
import com.smlaurindo.realtime_polls.dto.CreatePollRequest;
import com.smlaurindo.realtime_polls.dto.ListPollsResponse;
import com.smlaurindo.realtime_polls.service.PollService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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

    @GetMapping("/polls")
    public ResponseEntity<Page<ListPollsResponse>> listPolls(
            @RequestParam(required = false) PollStatus status,
            @PageableDefault(size = 20, sort = "startsAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        var polls = pollService.listPolls(status, pageable);

        return ResponseEntity.ok(polls);
    }
}
