package com.example.backend.socket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

@Slf4j
@Component
@Scope("prototype")
public class GestureLabelHandler extends BaseHandler {
    @Override
    protected void startTask(String childId) {
        new Thread(() -> {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(new URL(pythonUrl + "/gesture/predict?childId=" + childId).openStream()))) {
                String line;
                log.info("Session is opened, sending currentLabel to React");
                while ((line = in.readLine()) != null) {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(line));
                    } else {
                        log.info("Session gesture label is closed");
                        break;
                    }
                }
            } catch (Exception e) {
                log.error("Error fetching gesture label: ", e);
                try {
                    session.sendMessage(new TextMessage("Error fetching gesture label: " + e.getMessage()));
                } catch (IOException ioException) {
                    log.error("Error sending message to client: ", ioException);
                }
            }
        }).start();
    }
}
