package com.smlaurindo.realtime_polls.controller;

import com.smlaurindo.realtime_polls.domain.PollStatus;
import com.smlaurindo.realtime_polls.dto.*;
import com.smlaurindo.realtime_polls.service.PollService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PollController {

    private final PollService pollService;

    @PostMapping("/polls")
    public ResponseEntity<CreatePollResponse> createPoll(
            @RequestBody @Valid CreatePollRequest request
    ) {
        var poll = pollService.createPoll(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(poll);
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

    @PutMapping("/polls/{pollId}")
    public ResponseEntity<EditPollResponse> editPoll(
            @PathVariable("pollId") String pollId,
            @RequestBody @Valid EditPollRequest request
    ) {
        var poll = pollService.editPoll(pollId, request);

        return ResponseEntity.ok(poll);
    }

    @PostMapping("/polls/{pollId}/options")
    public ResponseEntity<AddPollOptionResponse> addPollOption(
            @PathVariable("pollId") String pollId,
            @RequestBody @Valid AddPollOptionRequest request
    ) {
        var pollOption = pollService.addPollOption(pollId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(pollOption);
    }

    @PatchMapping("/polls/{pollId}/options/{optionId}/vote")
    public ResponseEntity<Void> votePollOption(
            @PathVariable("pollId") String pollId,
            @PathVariable("optionId") String optionId
    ) {
        pollService.votePollOption(pollId, optionId);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/polls/{pollId}/options/{optionId}")
    public ResponseEntity<Void> deletePollOption(
            @PathVariable("pollId") String pollId,
            @PathVariable("optionId") String optionId
    ) {
        pollService.deletePollOption(pollId, optionId);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/polls/{pollId}")
    public ResponseEntity<Void> deletePoll(@PathVariable("pollId") String pollId) {
        pollService.deletePoll(pollId);

        return ResponseEntity.noContent().build();
    }
}
