package com.example.backend.dto;

import com.example.backend.model.VideoEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class VideoDTO {
    private String videoId;
    private String fileName;
    private String label;

    public VideoDTO(VideoEntity entity) {
        this.videoId = entity.getVideoId();
        this.fileName = entity.getFileName();
        this.label = entity.getLabel();
    }
}
