package com.example.backend.socket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

@Slf4j
@Component
@Scope("prototype")
public class VideoStatusHandler extends BaseVideoHandler {
    @Override
    protected void startVideoTask(String childId) {
        new Thread(() -> {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(new URL(pythonUrl + "/video/status?childId=" + childId).openStream()))) {
                String line;
                log.info("Session is opened, sending currentLabel to React");
                while ((line = in.readLine()) != null) {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(line));
                    } else {
                        log.info("Session status is closed");
                        break;
                    }
                }
            } catch (Exception e) {
                log.error("Error fetching video status: ", e);
                try {
                    session.sendMessage(new TextMessage("Error fetching video status: " + e.getMessage()));
                } catch (IOException ioException) {
                    log.error("Error sending message to client: ", ioException);
                }
            }
        }).start();
    }
}
