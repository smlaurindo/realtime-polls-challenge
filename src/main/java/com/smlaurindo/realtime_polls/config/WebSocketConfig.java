package com.smlaurindo.realtime_polls.config;

import com.smlaurindo.realtime_polls.handler.PollWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.List;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final PollWebSocketHandler pollWebSocketHandler;

    @Value("#{'${app.cors.allowed.origins}'.split(',')}")
    private List<String> allowedOrigins;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(pollWebSocketHandler, "/ws/polls/{pollId}")
                .setAllowedOrigins(allowedOrigins.toArray(new String[0]));
    }
}

