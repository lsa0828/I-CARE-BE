package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class DiaryMaker {
    private String diary;
    private String fileName;

    public DiaryMaker(String diary) {
        this.diary = diary;
    }
}
