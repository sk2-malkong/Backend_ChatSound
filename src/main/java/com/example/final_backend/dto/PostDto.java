package com.example.final_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class PostDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private String title;
        private String content;
        private String image;
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
        private String image;
        private int count;
        private int commentCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchRequest {
        private String keyword;
    }
}