package com.example.final_backend.dto;

import com.example.final_backend.entity.CommentEntity;
import com.example.final_backend.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 댓글 작성, 수정, 조회 시 사용하는 Dto
 */

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
    public static class WriteCommentResponse {
        private int commentId;
        private String userId;
        private String username;
        private String content;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        // 프론트 전달용 데이터
        private LocalDateTime endDate;  // 제한 끝나는 시간
        private Boolean isActive;       // 사용자 활동 여부

        // 정적 팩토리 메서드 추가
        public static WriteCommentResponse of(CommentEntity comment, UserEntity user) {
            LocalDateTime endDate = (user.getLimits() != null) ? user.getLimits().getEndDate() : null;
            Boolean isActive = (user.getLimits() != null) ? user.getLimits().getIsActive() : true;

            return WriteCommentResponse.builder()
                    .commentId(comment.getCommentId())
                    .userId(user.getId())
                    .username(user.getUsername())
                    .content(comment.getContent())
                    .createdAt(comment.getCreatedAt())
                    .updatedAt(comment.getUpdatedAt())
                    .endDate(endDate)
                    .isActive(isActive)
                    .build();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CheckCommentResponse {
        private int commentId;
        private String userId;
        private String username;
        private String content;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static CheckCommentResponse of(CommentEntity comment) {
            return CheckCommentResponse.builder()
                    .commentId(comment.getCommentId())
                    .userId(comment.getUser().getId())
                    .username(comment.getUser().getUsername())
                    .content(comment.getContent())
                    .createdAt(comment.getCreatedAt())
                    .updatedAt(comment.getUpdatedAt())
                    .build();
        }
    }
}