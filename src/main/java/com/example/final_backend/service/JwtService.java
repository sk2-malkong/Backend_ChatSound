package com.example.final_backend.service;

import com.example.final_backend.config.JwtConfig;
import com.example.final_backend.entity.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT 토큰 생성 및 검증
 * - AccessToken, RefreshToken 생성
 * - AccessToken, RefreshToken 검증
 * - AccessToken, RefreshToken 삭제 및 블랙리스트(기간제) 등록
 * - AccessToken 재발급
 */

@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtConfig jwtConfig;
    private final RedisService redisService;
    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    // AccessToken 발급
    public String generateAccessToken(UserEntity userEntity) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", userEntity.getUsername());
        claims.put("email", userEntity.getEmail());
        claims.put("tokenType", "access");

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userEntity.getId())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtConfig.getExpirationMs()))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // RefreshToken 발급 및 Redis에 저장
    public String generateRefreshToken(UserEntity userEntity) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenType", "refresh");

        String refreshToken = Jwts.builder()
                .setClaims(claims)
                .setSubject(userEntity.getId())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtConfig.getRefreshExpirationMs()))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // Redis에 RefreshToken 저장
        redisService.saveRefreshToken(userEntity.getId(), refreshToken, jwtConfig.getRefreshExpirationMs());

        return refreshToken;
    }

    // 토큰에서 subject(ID) 추출
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("tokenType", String.class));
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // 토큰 유효성 및 Redis 블랙리스트 여부 확인
    public Boolean validateAccessToken(String token, UserDetails userDetails) {
        final String userId = extractUsername(token);
        final String tokenType = extractTokenType(token);

        // AccessToken 블랙리스트 확인
        if (redisService.isAccessTokenBlacklisted(userId, token)) {
            return false;
        }

        return (userId.equals(userDetails.getUsername()) &&
                "access".equals(tokenType) &&
                !isTokenExpired(token));
    }

    // 토큰과 Redis 저장 토큰 비교, 만료 여부 확인
    public Boolean validateRefreshToken(String token, String userId) {
        try {
            final String tokenUserId = extractUsername(token);
            final String tokenType = extractTokenType(token);

            // Redis에서 저장된 refreshToken 가져오기
            String storedToken = redisService.getRefreshToken(userId);

            return (tokenUserId.equals(userId) &&
                    "refresh".equals(tokenType) &&
                    !isTokenExpired(token) &&
                    token.equals(storedToken));
        } catch (Exception e) {
            return false;
        }
    }

    // 토큰 재발급 (RefreshToken을 사용해 AccessToken 재발급)
    public String reissueAccessToken(UserEntity userEntity) {
        return generateAccessToken(userEntity);
    }

    // 로그아웃 처리 (RefreshToken 삭제 및 AccessToken 블랙리스트 등록)
    public void logout(String userId, String accessToken) {
        // RefreshToken 삭제
        redisService.deleteRefreshToken(userId);

        // AccessToken 블랙리스트에 추가 (만료시간까지만)
        long expirationTime = extractExpiration(accessToken).getTime() - System.currentTimeMillis();
        if (expirationTime > 0) {
            redisService.blacklistAccessToken(userId, accessToken, expirationTime);
        }
    }
}