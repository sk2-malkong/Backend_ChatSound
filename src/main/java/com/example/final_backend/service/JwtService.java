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
 * AccessToken / RefreshToken 생성 (generateAccessToken, generateRefreshToken)
 * JWT의 claim 추출 (extractUsername, extractExpiration 등)
 * 토큰 유효성 검사 (validateAccessToken, validateRefreshToken)
 * Redis 연동을 통한 RefreshToken 저장 및 AccessToken 블랙리스트 처리
 * 토큰 기반 로그아웃 처리 (logout)
 * 재발급 지원 (reissueAccessToken)
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

    // AccessToken 생성기
    public String generateAccessToken(UserEntity user) {

        Map<String, Object> claims = new HashMap<>();

        claims.put("username", user.getUsername());
        claims.put("email", user.getEmail());
        claims.put("tokenType", "access");

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getId())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtConfig.getExpirationMs()))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // RefreshToken 발급 및 Redis에 저장
    public String generateRefreshToken(UserEntity user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenType", "refresh");

        String refreshToken = Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getId())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtConfig.getRefreshExpirationMs()))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // Redis에 RefreshToken 저장
        redisService.saveRefreshToken(user.getId(), refreshToken, jwtConfig.getRefreshExpirationMs());

        return refreshToken;
    }


    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }


    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("tokenType", String.class));
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // 	JWT에서 원하는 Claim 값을 추출하는 범용 메서드
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);  // JWT 파싱 + 서명 검증 후 Claims 객체 반환
        return claimsResolver.apply(claims);    // 추출 전략 함수 적용
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)     // 서명 검증을 위한 SecretKey 설정
                .build()        // JwtParser 객체 생성
                .parseClaimsJws(token)      // JWT 문자열 파싱 + 서명 검증 수행
                .getBody();     // Payload(Payload = Claims) 반환
    }

    // JWT의 만료 시간을 읽어서, 현재 시간이 만료 시각을 지났는지 판단
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

        // 모든 조건이 충족되면 true 반환 → 인증 허용
        return (userId.equals(userDetails.getUsername()) && // JWT에 담긴 사용자 ID와 현재 인증 대상의 ID가 일치하는지 확인
                "access".equals(tokenType) && // 이 토큰이 AccessToken인지 확인
                !isTokenExpired(token)); //	토큰 만료 여부 검사 : 아직 유효한 토큰이면 true, 만료된 경우 false
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
    public String reissueAccessToken(UserEntity user) {
        // 엑세스 토큰 생성
        return generateAccessToken(user);
    }

    // 로그아웃 처리 (RefreshToken 삭제 및 AccessToken 블랙리스트 등록)
    public void logout(String userId, String accessToken) {
        // Redis에 저장된 userId의 RefreshToken을 삭제
        redisService.deleteRefreshToken(userId);

        long expirationTime = extractExpiration(accessToken).getTime() - System.currentTimeMillis();
        if (expirationTime > 0) {
            redisService.blacklistAccessToken(userId, accessToken, expirationTime);
        }
    }
}