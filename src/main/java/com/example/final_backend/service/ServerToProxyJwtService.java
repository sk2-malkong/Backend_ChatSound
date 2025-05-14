package com.example.final_backend.service;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 커뮤니티 서버에서 프록시 서버로 요청을 보낼 때 사용하는 JWT 기반 인증 토큰 생성 전용 서비스
 * - 욕설 필터링 API와 통신할 때 사용
 */

@Service
@RequiredArgsConstructor
public class ServerToProxyJwtService {

    @Value("${server-to-proxy.jwt.secret}")
    private String secretKeyString;

    @Value("${server-to-proxy.jwt.expiration}")
    private long expirationMillis;

    private Key secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes());
    }

    // JSON 문자열 만드는 메서드
    public String createJsonBody(Map<String, String> requestBodyMap) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        mapper.configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, false);

        return mapper.writeValueAsString(requestBodyMap);
    }

    // JSON 문자열을 받아서 JWT를 생성하는 메서드
    public String generateTokenFromJson(String jsonBody) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        Date expiryDate = new Date(nowMillis + expirationMillis);

        String bodyHash = DigestUtils.sha256Hex(jsonBody);

        Map<String, Object> claims = new HashMap<>();
        claims.put("iss", "purgo-skfinal");
        claims.put("hash", bodyHash);

        String jwt = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();

        System.out.println("🔵 발급한 JWT: " + jwt);
        System.out.println("🔵 직렬화된 JSON 본문: " + jsonBody);

        return jwt;
    }
}
