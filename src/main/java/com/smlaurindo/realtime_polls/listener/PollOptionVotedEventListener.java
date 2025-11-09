package com.smlaurindo.realtime_polls.listener;

import com.smlaurindo.realtime_polls.message.VoteUpdateMessage;
import com.smlaurindo.realtime_polls.event.PollOptionVotedEvent;
import com.smlaurindo.realtime_polls.message.WebSocketMessage;
import com.smlaurindo.realtime_polls.repository.OptionRepository;
import com.smlaurindo.realtime_polls.handler.PollWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Component
@RequiredArgsConstructor
public class PollOptionVotedEventListener {

    private final PollWebSocketHandler webSocketHandler;
    private final OptionRepository optionRepository;

    @Async
    @Transactional(propagation = REQUIRES_NEW)
    @TransactionalEventListener
    public void onPollOptionVoted(PollOptionVotedEvent event) {
        var option = optionRepository.findById(event.getOptionId())
                .orElseThrow();

        VoteUpdateMessage voteUpdatedMessage = new VoteUpdateMessage(
                option.getId(),
                option.getText(),
                option.getVotes()
        );

        var message = new WebSocketMessage<>(
                "VOTE_UPDATED",
                voteUpdatedMessage,
                event.getEventTimestamp().toString()
        );

        webSocketHandler.sendVoteUpdate(event.getPollId(), message);
    }
}
