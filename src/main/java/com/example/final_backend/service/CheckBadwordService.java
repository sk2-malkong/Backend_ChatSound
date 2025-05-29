package com.example.final_backend.service;

import com.example.final_backend.entity.BadwordLogEntity;
import com.example.final_backend.entity.CommentEntity;
import com.example.final_backend.entity.PostEntity;
import com.example.final_backend.entity.UserEntity;
import com.example.final_backend.repository.BadwordLogRepository;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 게시글 작성, 수정 | 댓글 작성 수정 시 비속어 사용 감지 및 대체어 변환
 * Proxy 서버로 전달
 */
@Service
@RequiredArgsConstructor
public class CheckBadwordService {
    private final RestTemplate purgoRestTemplate;
    private final ServerToProxyJwtService serverToProxyJwtService;
    private final UserPenaltyService userPenaltyService;
    private final BadwordLogRepository badwordLogRepository;

    @Value("${proxy.base-url}")
    private String baseUrl;

    @Value("${proxy.client-api-key}")
    private String apiKey;

    @Transactional
    public String getFilteredText(String text, UserEntity user, PostEntity post) {
        return getFilteredText(text, user, post, null);
    }

    @Transactional
    public String getFilteredText(String text, UserEntity user, PostEntity post, CommentEntity comment) {
        try {
            // 요청 본문 구성
            Map<String, String> body = new HashMap<>();
            body.put("text", text);

            String jsonBody = serverToProxyJwtService.createJsonBody(body);
            String serverJwt = serverToProxyJwtService.generateTokenFromJson(jsonBody);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("X-Auth-Token", serverJwt);

            HttpEntity<String> http = new HttpEntity<>(jsonBody, headers);

            // 프록시 서버로 요청
            ResponseEntity<Map<String, Object>> response = purgoRestTemplate.postForEntity(
                    baseUrl, http, (Class<Map<String, Object>>) (Class<?>) Map.class
            );

            // 응답 처리
            System.out.println("📦 [응답 바디] " + response.getBody());

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> result = response.getBody();
                Object decision = result.get("final_decision");
                Boolean isAbusive = decision != null && decision.toString().equals("1");

                Map<String, Object> resultInner = (Map<String, Object>) result.get("result");
                String rewritten = resultInner != null ? (String) resultInner.get("rewritten_text") : text;

                if (Boolean.TRUE.equals(isAbusive)) {
                    BadwordLogEntity log = BadwordLogEntity.of(user, post, comment, text, rewritten);
                    badwordLogRepository.save(log);
                    userPenaltyService.applyPenalty(user.getUserId());

                    return rewritten;
                }
            }
        } catch (Exception e) {
            System.out.println("❌ 욕설 필터링 실패");
            e.printStackTrace(); // 에러 로그 전체 출력
        }

        return text;
    }
}