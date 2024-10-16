package com.example.backend.dto;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class GestureCheck {
    private String cor_label;
    private Boolean check1;
    private Boolean check2;
}
