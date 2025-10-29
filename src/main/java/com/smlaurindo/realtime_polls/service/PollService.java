package com.smlaurindo.realtime_polls.service;

import com.smlaurindo.realtime_polls.domain.Option;
import com.smlaurindo.realtime_polls.domain.Poll;
import com.smlaurindo.realtime_polls.dto.CreatePollRequest;
import com.smlaurindo.realtime_polls.repository.PollRepository;
import lombok.RequiredArgsConstructor;
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
}
