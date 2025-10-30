package com.smlaurindo.realtime_polls.service;

import com.smlaurindo.realtime_polls.domain.Option;
import com.smlaurindo.realtime_polls.domain.Poll;
import com.smlaurindo.realtime_polls.domain.PollStatus;
import com.smlaurindo.realtime_polls.dto.CreatePollRequest;
import com.smlaurindo.realtime_polls.dto.ListPollsResponse;
import com.smlaurindo.realtime_polls.repository.PollRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PollService {

    private final PollRepository pollRepository;

    @Transactional
    public Poll createPoll(CreatePollRequest request) {
        var poll = Poll.builder()
                .question(request.question())
                .startsAt(request.startsAt().toInstant())
                .endsAt(request.endsAt().toInstant())
                .build();

        List<Option> options = request.options().stream()
                .map(text -> Option.builder()
                        .text(text)
                        .poll(poll)
                        .build()
                ).toList();

        poll.setOptions(options);

        return pollRepository.save(poll);
    }

    @Transactional(readOnly = true)
    public Page<ListPollsResponse> listPolls(PollStatus status, Pageable pageable) {
        Page<Poll> pollPage = switch (status) {
            case NOT_STARTED -> pollRepository.findAllNotStarted(pageable);
            case IN_PROGRESS -> pollRepository.findAllInProgress(pageable);
            case FINISHED -> pollRepository.findAllFinished(pageable);
            case null -> pollRepository.findAll(pageable);
        };

        return pollPage.map((poll) -> new ListPollsResponse(
                poll.getId(),
                poll.getQuestion(),
                poll.getStatus(),
                poll.getStartsAt().toString(),
                poll.getEndsAt().toString(),
                poll.getOptions()
                        .stream()
                        .map(option -> new ListPollsResponse.OptionResponse(
                                option.getId(),
                                option.getText(),
                                option.getVotes()
                        )).toList()
        ));
    }
}
