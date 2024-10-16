package com.example.backend.dto;

import com.example.backend.model.GestureEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class GestureDTO {
    private String gestureId;
    private String fileName;
    private String label;

    public GestureDTO(GestureEntity entity) {
        this.gestureId = entity.getGestureId();
        this.fileName = entity.getFileName();
        this.label = entity.getLabel();
    }
}
