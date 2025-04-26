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

    //애플리케이션 시작 직후 1번만 최초로 실행됨
    // 매 jwt 요청마다 만들면 비효율 + 성능 저하
    // 애플리케이션이 켜질 때 한 번만 키를 생성해두고 계속 재사용하는 구조
    //jwt.secret 문자열을 HMAC 서명용 SecretKey 객체로 변환하여 저장하는데
    // 이게 JWT 생성시 반드시 필요한 key임
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    // AccessToken 생성기
    public String generateAccessToken(UserEntity userEntity) {
        //클라이언트가 디코딩하면 볼 수 있는 공개 정보
        // Map<String, Object>가 payload임
        Map<String, Object> claims = new HashMap<>();
        //프론트에서 필요 시 토큰 decode하여 화면에 표시 가능 (ex: 사용자명 표시)
        claims.put("username", userEntity.getUsername());
        claims.put("email", userEntity.getEmail());
        claims.put("tokenType", "access");
            // JWT 빌드 시작
        return Jwts.builder()
                //위에서 설정한 사용자 정보들 추가
                .setClaims(claims)
                //토큰의 subject 필드에는 사용자 ID 저장
                .setSubject(userEntity.getId())
                //토큰 발행 시간
                .setIssuedAt(new Date(System.currentTimeMillis()))
                //만료 시간 설정 – expirationMs는 application.properties에서 설정
                .setExpiration(new Date(System.currentTimeMillis() + jwtConfig.getExpirationMs()))
                .signWith(key, SignatureAlgorithm.HS256) // 서명임. Signature이 생성됨.
                //위에서 구성한 Header, Payload, Signature를
                //Base64UrlEncoding 형식으로 인코딩한 후
                //.(점)으로 연결해서 최종 문자열 생성
                .compact(); //JWT 완성 → 문자열화
    }

    // RefreshToken 발급 및 Redis에 저장
    public String generateRefreshToken(UserEntity userEntity) {
        Map<String, Object> claims = new HashMap<>();
        // 토큰을 구분하기 위해 tokenType만 넣음
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

    // 밑에 있는 메서드들은 AccessToken 또는
    // RefreshToken 안에 들어 있는 정보를 꺼내기 위해 사용됨

    // 토큰에서 subject(ID) 추출
    // 인증된 사용자가 누구인지 식별하기 위해 필요하기 때문
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // JWT에서 우리가 커스텀 Claim으로 넣은 "tokenType" 값을 추출
    // AccessToken/RefreshToken 구분을 위해 사용
    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("tokenType", String.class));
    }

    // JWT의 만료 시간(exp) 을 가져오는 메서드
    // 현재 시간과 비교해서 이 토큰이 만료됐는지 판단하기 위함
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // 	JWT에서 원하는 Claim 값을 추출하는 범용 메서드
    // 위 세 메서드에서 실제 Claim을 추출해주는 공통 로직
    // 전달받은 함수(claimsResolver)를 적용해 특정 Claim 값을 반환
    // <T>를 통해 String, Date, Boolean 등 다양한 반환 타입 지원
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        //토큰에서 서명을 검증하고 Payload 부분인 Claims 객체를 추출합니다.
        //내부적으로 extractAllClaims()를 호출하여 JWT 디코딩 및 파싱 수행
        final Claims claims = extractAllClaims(token);  // JWT 파싱 + 서명 검증 후 Claims 객체 반환
        return claimsResolver.apply(claims);    // 추출 전략 함수 적용
    }

    // JWT 문자열을 파싱하고 서명을 검증한 후, Payload(Claims)만 반환
    // 서버가 JWT의 Payload를 안전하게 읽기 위해 사용
    // getBody()로 subject, custom claims, expiration 등의 데이터 복원
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

        // extractUsername,extractTokenType가 파싱이 되기 전이라고
        // 생각할 수 있지만 내부적으로 extractClaim를 실행해
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
    // 입력값으로 UserEntity 객체를 받아서,
    //해당 사용자의 정보를 기반으로 JWT를 다시 생성합니다.
    // reissueAccessToken는 매서드임
    public String reissueAccessToken(UserEntity userEntity) {
        // generateAccessToken 매서드에 userEntity를 넣어서
        // 엑세스 토큰 생성함
        // authservice에 줌
        return generateAccessToken(userEntity);
    }

    // 로그아웃 처리 (RefreshToken 삭제 및 AccessToken 블랙리스트 등록)
    // logout 메서드
    public void logout(String userId, String accessToken) {
        // Redis에 저장된 userId의 RefreshToken을 삭제
        redisService.deleteRefreshToken(userId);

        // AccessToken 블랙리스트에 추가 (만료시간까지만)
        // extractExpiration 메서드를 호출해서 엑세스 토큰의 만료 시간을 추출
        // .getTime으로 를 사용해서 ms 단위로 변경
        // System.currentTimeMillis는 현재 시간을 호출
        // 둘을 빼서 만료까지 남은 시간을 계산
        long expirationTime = extractExpiration(accessToken).getTime() - System.currentTimeMillis();
        // 토큰이 만료되지 않았을 경우 블랙리스트 등록
        // 이미 만료된 토큰을 등록하는건 불필요한 redis 낭비
        // 엑세스 토큰이 만료되지 않고 존재하는 상태에서 블랙리스트에 넣지 않으면
        // 무한히 생성되기 때문에 막아야함
        if (expirationTime > 0) {
            redisService.blacklistAccessToken(userId, accessToken, expirationTime);
        }
    }
}