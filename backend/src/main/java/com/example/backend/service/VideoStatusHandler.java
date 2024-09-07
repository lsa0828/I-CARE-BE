package com.example.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Component
@Scope("prototype")
public class VideoStatusHandler extends TextWebSocketHandler {
    @Value("${python.connected.url}")
    private String pythonUrl;
    private WebSocketSession session;
    private String childId;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        this.session = session;
        this.childId = (String) session.getAttributes().get("childId");
        if (childId != null) {
            startFetchingVideoStatus();
        } else {
            session.sendMessage(new TextMessage("Error: childId not provided"));
            session.close();
        }
    }

    private void startFetchingVideoStatus() {
        new Thread(() -> {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(new URL(pythonUrl + "/video/status?childId=" + childId).openStream()))) {
                String line;
                while ((line = in.readLine()) != null) {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(line));
                    } else {
                        break;
                    }
                }
            } catch (Exception e) {
                try {
                    session.sendMessage(new TextMessage("Error: " + e.getMessage()));
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }).start();
    }
}
