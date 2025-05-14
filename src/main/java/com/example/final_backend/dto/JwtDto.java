package com.example.final_backend.dto;

import com.example.final_backend.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 토큰 발급 관련 DTO 모음
 */

public class JwtDto {

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

        // 정적 팩토리 메서드 추가
        public static TokenResponse of(UserEntity user, String accessToken, String refreshToken) {
            return TokenResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .id(user.getId())
                    .build();
        }
    }

    // 토큰 재발급 요청
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RefreshTokenRequest {
        private String refreshToken;
    }
}
