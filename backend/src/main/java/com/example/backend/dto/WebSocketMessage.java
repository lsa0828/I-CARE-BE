package com.example.backend.dto;

import lombok.Getter;

@Getter
public class WebSocketMessage {
    private String childId;
    private String token;
}
