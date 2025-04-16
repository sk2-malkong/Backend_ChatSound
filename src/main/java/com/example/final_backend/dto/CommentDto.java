package com.example.final_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class CommentDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private String content;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private int commentId;
        private String userId;
        private String username;
        private String content;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}