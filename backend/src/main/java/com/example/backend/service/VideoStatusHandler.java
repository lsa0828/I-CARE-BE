package com.example.backend.service;

import com.example.backend.dto.WebSocketMessage;
import com.example.backend.repository.ChildRepository;
import com.example.backend.security.TokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

@Slf4j
@Component
@Scope("prototype")
public class VideoStatusHandler extends TextWebSocketHandler {
    @Value("${python.connected.url}")
    private String pythonUrl;
    private WebSocketSession session;
    @Autowired
    private TokenProvider tokenProvider;
    @Autowired
    private ChildRepository childRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        this.session = session;
        log.info("WebSocket connection established with session id: {}", session.getId());
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("Received: " + payload);

        WebSocketMessage webSocketMessage = objectMapper.readValue(payload, WebSocketMessage.class);
        String childId = webSocketMessage.getChildId();
        String token = webSocketMessage.getToken();
        if(token != null) {
            String parentId = tokenProvider.validateAndGetParentId(token);
            validate(parentId, childId);
            startFetchingVideoStatus(childId);
        } else {
            this.session.sendMessage(new TextMessage("Invalid token"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("WebSocket connection closed. Session id: {}, Status: {}", session.getId(), status);
    }

    private void startFetchingVideoStatus(String childId) {
        new Thread(() -> {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(new URL(pythonUrl + "/video/status?childId=" + childId).openStream()))) {
                String line;
                while ((line = in.readLine()) != null) {
                    if (session.isOpen()) {
                        log.info("Session is opened, sending currentLabel to React");
                        session.sendMessage(new TextMessage(line));
                    } else {
                        log.info("Session is closed");
                        break;
                    }
                }
            } catch (Exception e) {
                log.error("Error fetching video status: ", e);
                try {
                    session.sendMessage(new TextMessage("Error: " + e.getMessage()));
                } catch (IOException ioException) {
                    log.error("Error sending message to client: ", ioException);
                }
            }
        }).start();
    }

    public void validate(String parentId, String childId) {
        if (!parentId.equals(childRepository.findByChildId(childId).getParentId())) {
            log.error("Child's parent and current parent do not match.");
            throw new RuntimeException("Child's parent and current parent do not match.");
        }
    }
}
