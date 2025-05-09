package com.example.final_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 로그인 / 토큰 재발급 등에 사용하는 DTO 모음
 */

public class JwtDto {

    // 로그인 요청
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LoginRequest {
        private String id;
        private String pw;
    }

    // JWT 응답 (Access, Refresh)
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TokenResponse {
        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private String id;
        private String username;

        // 프론트 전달용 데이터
        private LocalDateTime endDate;  // endDate 전달
        private Boolean isActive;       // 사용자 활동 여부
    }

    // 토큰 재발급 요청
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RefreshTokenRequest {
        private String refreshToken;
    }
}
