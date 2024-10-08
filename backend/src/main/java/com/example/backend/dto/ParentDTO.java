package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParentDTO {
    private String token;
    private String parentId;
    private String email;
    private String password;
    private String nickname;
    private String address;
}
