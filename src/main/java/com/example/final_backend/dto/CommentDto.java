package com.example.final_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class CommentDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentRequest {
        private String content;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CommentResponse {
        private int commentId;
        private String userId;
        private String username;
        private String content;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        // 프론트 전달용 데이터
        private LocalDateTime endDate;  // 제한 끝나는 시간
        private int penaltyCount;       // 패널티 횟수
    }
}