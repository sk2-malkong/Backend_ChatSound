package com.example.final_backend.dto;

import com.example.final_backend.entity.UserEntity;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 회원가입, 정보 수정, 로그인 시 사용하는 DTO
 */

@Getter
@Setter
public class AuthDto {
    private String id;
    private String username;
    private String email;
    private String pw;
    private String profileImage;

    // 로그인 요청
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LoginRequest {
        private String id;
        private String pw;
    }

    // 로그인 응답 전용 DTO
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LoginResponse {
        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private String id;
        private String username;
        private LocalDateTime endDate;
        private Boolean isActive;

        // 정적 팩토리 메서드 추가
        public static LoginResponse of(UserEntity user, String accessToken, String refreshToken, LocalDateTime endDate, Boolean isActive) {
            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .id(user.getId())
                    .username(user.getUsername())
                    .endDate(endDate)
                    .isActive(isActive)
                    .build();
        }
    }
}
