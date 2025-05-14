package com.example.final_backend.dto;

import com.example.final_backend.entity.UserEntity;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 프로필 조회 시 사용하는 Dto
 */

public class ProfileDto {
    // 프로필 조회 응답용 DTO
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserProfile {
        private int userId;
        private String id;
        private String username;
        private String email;
        private String profileImage;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime endDate;
        private Boolean isActive;

        public static UserProfile of(UserEntity user) {
            LocalDateTime endDate = null;
            Boolean isActive = true;
            if (user.getLimits() != null) {
                endDate = user.getLimits().getEndDate();
                isActive = user.getLimits().getIsActive();
            }

            return UserProfile.builder()
                    .userId(user.getUserId())
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .profileImage(user.getProfileImage())
                    .createdAt(user.getCreatedAt())
                    .updatedAt(user.getUpdatedAt())
                    .endDate(endDate)
                    .isActive(isActive)
                    .build();
        }
    }

    // 프로필 수정 요청용 DTO
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateProfile {
        private String username;
        private String profileImage;

        public void UpdateProfile(UserEntity user) {
            user.setUsername(this.username);
            user.setProfileImage(this.profileImage);
            user.setUpdatedAt(LocalDateTime.now());
        }
    }
}
