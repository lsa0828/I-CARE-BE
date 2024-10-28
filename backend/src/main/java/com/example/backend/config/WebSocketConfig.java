package com.example.backend.config;

import com.example.backend.socket.GestureLabelHandler;
import com.example.backend.socket.GestureStreamHandler;
import com.example.backend.socket.VideoStatusHandler;
import com.example.backend.socket.VideoStreamHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Autowired
    private ApplicationContext context;
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(context.getBean(VideoStatusHandler.class), "/ws/video/status")
                .addHandler(context.getBean(VideoStreamHandler.class), "/ws/video/stream")
                .addHandler(context.getBean(GestureStreamHandler.class), "/ws/gesture/stream")
                .addHandler(context.getBean(GestureLabelHandler.class), "ws/gesture/label")
                .setAllowedOrigins("*")
                .addInterceptors(new HttpSessionHandshakeInterceptor());
    }
}
