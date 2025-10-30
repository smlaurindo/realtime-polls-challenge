package com.smlaurindo.realtime_polls.service;

import com.smlaurindo.realtime_polls.domain.Option;
import com.smlaurindo.realtime_polls.domain.Poll;
import com.smlaurindo.realtime_polls.domain.PollStatus;
import com.smlaurindo.realtime_polls.dto.*;
import com.smlaurindo.realtime_polls.exception.InvalidPollDateException;
import com.smlaurindo.realtime_polls.exception.PollAlreadyStartedException;
import com.smlaurindo.realtime_polls.exception.ResourceNotFoundException;
import com.smlaurindo.realtime_polls.repository.OptionRepository;
import com.smlaurindo.realtime_polls.repository.PollRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PollService {

    private final PollRepository pollRepository;
    private final OptionRepository optionRepository;

    @Transactional
    public CreatePollResponse createPoll(CreatePollRequest request) {
        var poll = Poll.builder()
                .question(request.question())
                .startsAt(request.startsAt().toInstant())
                .endsAt(request.endsAt().toInstant())
                .build();

        pollRepository.save(poll);

        List<Option> options = request.options().stream()
                .map(text -> Option.builder()
                        .text(text)
                        .poll(poll)
                        .build()
                ).toList();

        optionRepository.saveAll(options);

        poll.setOptions(options);

        return new CreatePollResponse(
                poll.getId(),
                poll.getQuestion(),
                poll.getStatus(),
                poll.getStartsAt().toString(),
                poll.getEndsAt().toString(),
                poll.getOptions().stream()
                        .map(option -> new CreatePollResponse.OptionResponse(
                                option.getId(),
                                option.getText(),
                                option.getVotes()
                        ))
                        .toList()
        );
    }

    @Transactional(readOnly = true)
    public Page<ListPollsResponse> listPolls(PollStatus status, Pageable pageable) {
        Page<Poll> pollPage = switch (status) {
            case NOT_STARTED -> pollRepository.findAllNotStarted(pageable);
            case IN_PROGRESS -> pollRepository.findAllInProgress(pageable);
            case FINISHED -> pollRepository.findAllFinished(pageable);
            case null -> pollRepository.findAll(pageable);
        };

        List<String> pollIds = pollPage.stream()
                .map(Poll::getId)
                .toList();

        List<Option> options = optionRepository.findByPollIds(pollIds);

        Map<String, List<Option>> optionsByPollId = options.stream()
                .collect(Collectors.groupingBy(option -> option.getPoll().getId()));

        return pollPage.map((poll) -> new ListPollsResponse(
                poll.getId(),
                poll.getQuestion(),
                poll.getStatus(),
                poll.getStartsAt().toString(),
                poll.getEndsAt().toString(),
                optionsByPollId.getOrDefault(poll.getId(), List.of())
                        .stream()
                        .map(option -> new ListPollsResponse.OptionResponse(
                                option.getId(),
                                option.getText(),
                                option.getVotes()
                        ))
                        .toList()
        ));
    }

    @Transactional
    public EditPollResponse editPoll(String pollId, EditPollRequest request) {
        var poll = pollRepository.findByIdWithOptions(pollId)
                .orElseThrow(() -> new ResourceNotFoundException("Poll with id " + pollId + " does not exist."));

        if (poll.getStatus() != PollStatus.NOT_STARTED) {
            throw new PollAlreadyStartedException("Poll cannot be edited after it has started.");
        }

        if (request.question() != null) poll.setQuestion(request.question());

        if (request.startsAt() != null) poll.setStartsAt(request.startsAt().toInstant());

        if (request.endsAt() != null) poll.setEndsAt(request.endsAt().toInstant());

        var start = poll.getStartsAt();
        var end = poll.getEndsAt();

        if (start != null && end != null && !end.isAfter(start)) {
            throw new InvalidPollDateException("The end date must be after the start date.");
        }

        pollRepository.save(poll);

        return new EditPollResponse(
                poll.getId(),
                poll.getQuestion(),
                poll.getStatus(),
                poll.getStartsAt().toString(),
                poll.getEndsAt().toString(),
                poll.getOptions().stream()
                        .map(option -> new EditPollResponse.OptionResponse(
                                option.getId(),
                                option.getText(),
                                option.getVotes()
                        ))
                        .toList()
        );
    }

    @Transactional
    public void deletePoll(String pollId) {
        var pollToDelete = pollRepository.existsById(pollId);

        if (!pollToDelete) {
            throw new ResourceNotFoundException("Poll with id " + pollId + " does not exist.");
        }

        pollRepository.deleteById(pollId);
    }
}
