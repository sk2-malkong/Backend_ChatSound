package com.example.final_backend.dto;

import lombok.*;

import java.time.LocalDateTime;

public class PostDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private String title;
        private String content;
        // private String image;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private int postId;
        private String userId;
        private String username;
        private String title;
        private String content;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        // String image;
        private int count;          // 조회수
        private int commentCount;   // 해당 게시글에 달린 댓글 수

        // 프론트 전달용 데이터
        private LocalDateTime endDate;  // 제한 끝나는 시간
        private int penaltyCount;       // 패널티 횟수
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchRequest {
        private String keyword;
    }

}