package com.example.backend.socket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;

@Slf4j
@Component
@Scope("prototype")
public class GestureStreamHandler extends BaseHandler {
    private volatile boolean isStreaming = false;
    private static final int BUFFER_SIZE = 1024 * 64;
    @Override
    protected void startTask(String childId) {
        isStreaming = true;
        new Thread(() -> {
            try (InputStream in = new URL(pythonUrl + "/gesture/stream?childId=" + childId).openStream()) {
                byte[] buffer = new byte[BUFFER_SIZE]; // 버퍼 크기
                int bytesRead;
                log.info("Session is opened, sending frame to React");
                while (isStreaming && (bytesRead = in.read(buffer)) != -1) {
                    if (session.isOpen()) {
                        // JPEG 시그니처 찾기
                        int jpegStart = -1;
                        for (int i = 0; i < bytesRead - 1; i++) {
                            if (buffer[i] == (byte)0xFF && buffer[i+1] == (byte)0xD8) {
                                jpegStart = i;
                                break;
                            }
                        }
                        if (jpegStart != -1) {
                            // JPEG 데이터만 전송
                            BinaryMessage message = new BinaryMessage(Arrays.copyOfRange(buffer, jpegStart, bytesRead));
                            session.sendMessage(message);
                        }
                    } else {
                        log.info("Session streaming is closed");
                        break;
                    }
                }
            } catch (Exception e) {
                log.error("Error during video streaming: ", e);
                handleStreamingError(e);
            } finally {
                stopStreaming();
            }
        }).start();
    }

    private void handleStreamingError(Exception e) {
        if(session.isOpen()) {
            try {
                session.sendMessage(new TextMessage("Error: " + e.getMessage()));
            } catch (IOException ioException) {
                log.error("Falied to send error message: ", ioException);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        stopStreaming();
    }

    private void stopStreaming() {
        isStreaming = false;
    }
}
