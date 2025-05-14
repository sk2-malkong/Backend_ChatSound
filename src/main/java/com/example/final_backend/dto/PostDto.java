package com.example.final_backend.dto;

import com.example.final_backend.entity.PostEntity;
import com.example.final_backend.entity.UserEntity;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 게시글 작성, 수정, 조회 시 사용하는 Dto
 */

public class PostDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostRequest {
        private String title;
        private String content;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WritePostResponse {
        private int postId;
        private String userId;
        private String username;
        private String title;
        private String content;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private int count;          // 조회수

        // 프론트 전달용 데이터
        private LocalDateTime endDate;  // 제한 끝나는 시간
        private Boolean isActive;       // 사용자 활동 여부

        public static WritePostResponse of(PostEntity post, UserEntity user) {
            LocalDateTime endDate = (user.getLimits() != null) ? user.getLimits().getEndDate() : null;
            Boolean isActive = (user.getLimits() != null) ? user.getLimits().getIsActive() : true;

            return WritePostResponse.builder()
                    .postId(post.getPostId())
                    .userId(user.getId())
                    .username(user.getUsername())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .createdAt(post.getCreatedAt())
                    .updatedAt(post.getUpdatedAt())
                    .count(post.getCount())
                    .endDate(endDate)
                    .isActive(isActive)
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CheckPostResponse {
        private int postId;
        private String userId;
        private String username;
        private String title;
        private String content;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private int count;          // 조회수
        private int commentCount;   // 해당 게시글에 달린 댓글 수

        public static CheckPostResponse of(PostEntity post) {
            return CheckPostResponse.builder()
                    .postId(post.getPostId())
                    .userId(post.getUser().getId())
                    .username(post.getUser().getUsername())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .createdAt(post.getCreatedAt())
                    .updatedAt(post.getUpdatedAt())
                    .count(post.getCount())
                    .commentCount(post.getCommentCount())
                    .build();
        }

    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CheckDetailsResponse {
        private int postId;
        private String userId;
        private String username;
        private String title;
        private String content;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private int count; // 조회수

        public static CheckDetailsResponse of(PostEntity post) {
            return CheckDetailsResponse.builder()
                    .postId(post.getPostId())
                    .userId(post.getUser().getId())
                    .username(post.getUser().getUsername())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .createdAt(post.getCreatedAt())
                    .updatedAt(post.getUpdatedAt())
                    .count(post.getCount())
                    .build();
        }
    }

}