package com.example.final_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * JWT 토큰 관리(특히 로그아웃 및 재발급)
 */

@Service
@RequiredArgsConstructor
public class RedisService {
    private final RedisTemplate<String, String> redisTemplate;

    private static final String ACCESS_TOKEN_PREFIX = "access:";
    private static final String REFRESH_TOKEN_PREFIX = "refresh:";

    // RefreshToken을 Redis에 저장
    public void saveRefreshToken(String userId, String refreshToken, long ttl) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        redisTemplate.opsForValue().set(key, refreshToken, ttl, TimeUnit.MILLISECONDS);
    }

    // AccessToken을 Redis에 저장 (블랙리스트 관리용)
    public void saveAccessToken(String userId, String accessToken, long ttl) {
        String key = ACCESS_TOKEN_PREFIX + userId;
        redisTemplate.opsForValue().set(key, accessToken, ttl, TimeUnit.MILLISECONDS);
    }

    // RefreshToken 조회
    public String getRefreshToken(String userId) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? value.toString() : null;
    }

    // AccessToken 블랙리스트 체크
    public boolean isAccessTokenBlacklisted(String userId, String accessToken) {
        String key = ACCESS_TOKEN_PREFIX + userId;
        Object value = redisTemplate.opsForValue().get(key);
        return value != null && value.toString().equals(accessToken);
    }

    // RefreshToken 삭제 (로그아웃 시)
    public void deleteRefreshToken(String userId) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        redisTemplate.delete(key);
    }

    // AccessToken 블랙리스트에 추가 (로그아웃 시)
    public void blacklistAccessToken(String userId, String accessToken, long ttl) {
        String key = ACCESS_TOKEN_PREFIX + userId;
        redisTemplate.opsForValue().set(key, accessToken, ttl, TimeUnit.MILLISECONDS);
    }
}