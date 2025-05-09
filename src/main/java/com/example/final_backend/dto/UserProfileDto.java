package com.example.final_backend.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class UserProfileDto {
    private int userId;
    private String id;
    private String username;
    private String email;
    private String profileImage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 프론트 전달용 데이터
    private LocalDateTime endDate;  // 제한 끝나는 시간
    private Boolean isActive;       // 사용자 활동 여부
}