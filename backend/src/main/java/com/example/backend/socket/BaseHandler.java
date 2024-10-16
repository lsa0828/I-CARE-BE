package com.example.backend.socket;

import com.example.backend.dto.WebSocketMessage;
import com.example.backend.repository.ChildRepository;
import com.example.backend.security.TokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@Component
@Scope("prototype")
public abstract class BaseHandler extends TextWebSocketHandler {
    @Value("${python.connected.url}")
    protected String pythonUrl;
    protected WebSocketSession session;
    @Autowired
    private TokenProvider tokenProvider;
    @Autowired
    private ChildRepository childRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        this.session = session;
        log.info("WebSocket connection established: " + session.getId());
    }

    protected abstract void startTask(String childId);

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        WebSocketMessage webSocketMessage = objectMapper.readValue(payload, WebSocketMessage.class);
        String childId = webSocketMessage.getChildId();
        String token = webSocketMessage.getToken();
        if(token != null) {
            String parentId = tokenProvider.validateAndGetParentId(token);
            validate(parentId, childId);
            startTask(childId);
        } else {
            this.session.sendMessage(new TextMessage("Invalid token"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("WebSocket connection closed: " + session.getId());
    }

    public void validate(String parentId, String childId) {
        if (!parentId.equals(childRepository.findByChildId(childId).getParentId())) {
            log.error("Child's parent and current parent do not match.");
            throw new RuntimeException("Child's parent and current parent do not match.");
        }
    }
}
