package com.smlaurindo.realtime_polls.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smlaurindo.realtime_polls.message.WebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;


import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@Slf4j
@RequiredArgsConstructor
public class PollWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;

    private final Map<String, CopyOnWriteArraySet<WebSocketSession>> pollSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String pollId = extractPollId(session);

        if (pollId == null) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        pollSessions
                .computeIfAbsent(pollId, _ -> new CopyOnWriteArraySet<>())
                .add(session);

        log.info("Session {} connected -> poll {}", session.getId(), pollId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String pollId = extractPollId(session);

        cleanupSession(pollId, session);

        log.info("Session {} disconnected from poll {}. Reason: {}",
                session.getId(), pollId, status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String pollId = extractPollId(session);

        cleanupSession(pollId, session);

        log.warn("Transport error on session {} -> poll {}: {}",
                session.getId(), pollId, exception.getMessage());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.debug("Received message from Session {}: {}", session.getId(), message.getPayload());
    }

    private void cleanupSession(String pollId, WebSocketSession session) {
        if (pollId == null) return;

        pollSessions.computeIfPresent(pollId, (id, sessions) -> {
            sessions.remove(session);
            return sessions.isEmpty() ? null : sessions;
        });
    }

    private String extractPollId(WebSocketSession session) {
        var uri = session.getUri();

        if (uri == null) return null;

        String[] segments = uri.getPath().split("/");

        if (segments.length == 0) return null;

        String candidate = segments[segments.length - 1];

        try {
            return UUID.fromString(candidate).toString();
        } catch (IllegalArgumentException e) {
            return null; // not a valid UUID
        }
    }

    public void sendVoteUpdate(String pollId, WebSocketMessage<?> message) {
        CopyOnWriteArraySet<WebSocketSession> sessions = pollSessions.get(pollId);

        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        try {
            String json = objectMapper.writeValueAsString(message);
            TextMessage textMessage = new TextMessage(json);

            sessions.removeIf(session -> {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(textMessage);
                        return false;
                    }
                } catch (Exception e) {
                    log.warn("Error sending message to session {}, removing...", session.getId());
                }

                return true;
            });

            log.info("Sent update to {} active sessions for poll {}", sessions.size(), pollId);
        } catch (Exception e) {
            log.error("Failed to broadcast update for poll {}: {}", pollId, e.getMessage());
        }
    }
}

