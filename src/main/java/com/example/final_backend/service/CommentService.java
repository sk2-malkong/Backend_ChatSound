package com.example.final_backend.service;

import com.example.final_backend.dto.CommentDto;
import com.example.final_backend.entity.BadwordLogEntity;
import com.example.final_backend.entity.CommentEntity;
import com.example.final_backend.entity.PostEntity;
import com.example.final_backend.entity.UserEntity;
import com.example.final_backend.repository.AuthRepository;
import com.example.final_backend.repository.BadwordLogRepository;
import com.example.final_backend.repository.CommentRepository;
import com.example.final_backend.repository.PostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final AuthRepository authRepository;
    private final BadwordLogRepository badwordLogRepository;
    private final UserService userService;
    private final RestTemplate purgoRestTemplate;
    private final ServerToProxyJwtService serverToProxyJwtService; // 🔧 서버 간 JWT 생성을 위한 서비스 추가

    @Value("${proxy.server.url}")
    private String gatewayUrl;

    @Value("${PURGO_CLIENT_API_KEY}")
    private String clientApiKey;

    // 🔧 욕설 필터링 + 로그 저장 로직 (FastAPI 프록시 호출)
    private String refineIfNeeded(String text, UserEntity user, PostEntity post, CommentEntity comment) {
        try {
            Map<String, String> body = new HashMap<>();
            body.put("text", text);

            // 🔧 JWT 생성
            String jsonBody = serverToProxyJwtService.createJsonBody(body);
            String serverJwt = serverToProxyJwtService.generateTokenFromJson(jsonBody);

            // 🔧 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + clientApiKey); // 클라이언트 API Key
            headers.set("X-Auth-Token", serverJwt);                 // 서버-프록시 JWT

            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            ResponseEntity<Map<String, Object>> response = purgoRestTemplate.postForEntity(
                    gatewayUrl, entity, (Class<Map<String, Object>>) (Class<?>) Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> result = response.getBody();

                Object decision = result.get("final_decision");
                Boolean isAbusive = decision != null && decision.toString().equals("1");

                Map<String, Object> resultInner = (Map<String, Object>) result.get("result");
                String rewritten = resultInner != null ? (String) resultInner.get("rewritten_text") : text;

                if (Boolean.TRUE.equals(isAbusive)) {
                    BadwordLogEntity log = new BadwordLogEntity();
                    log.setUser(user);
                    log.setPost(post);
                    log.setComment(comment);
                    log.setOriginalWord(text);
                    log.setFilteredWord(rewritten);
                    log.setCreatedAt(LocalDateTime.now());
                    badwordLogRepository.save(log);

                    userService.applyPenalty(user.getUserId());

                    return rewritten;
                }
            }
        } catch (Exception e) {
            System.out.println("❌ 욕설 분석 실패: " + e.getMessage());
        }
        return text;
    }

    // 댓글 조회
    public List<CommentDto.CommentResponse> getCommentsByPostId(int postId) {
        PostEntity post = postRepository.findByPostId(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        List<CommentEntity> comments = commentRepository.findByPost(post);
        List<CommentDto.CommentResponse> commentResponse = new ArrayList<>();

        for (CommentEntity comment : comments) {
            CommentDto.CommentResponse dto = new CommentDto.CommentResponse();
            dto.setCommentId(comment.getCommentId());
            dto.setUserId(comment.getUser().getId());
            dto.setUsername(comment.getUser().getUsername());
            dto.setContent(comment.getContent());
            dto.setCreatedAt(comment.getCreatedAt());
            dto.setUpdatedAt(comment.getUpdatedAt());
            commentResponse.add(dto);
        }
        return commentResponse;
    }

    // 댓글 생성
    @Transactional
    public void createComment(String userId, int postId, CommentDto.CommentRequest commentRequest) {
        UserEntity user = authRepository.findById(userId).orElseThrow();
        PostEntity post = postRepository.findById(postId).orElseThrow();

        userService.checkUserLimit(user);

        CommentEntity comment = new CommentEntity();
        comment.setUser(user);
        comment.setPost(post);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        comment.setContent(commentRequest.getContent());
        comment = commentRepository.save(comment);

        // 🔧 댓글 내용 정제
        String refined = refineIfNeeded(commentRequest.getContent(), user, post, comment);
        comment.setContent(refined);
        commentRepository.save(comment);
    }

    // 댓글 수정
    @Transactional
    public void updateComment(String userId, int commentId, CommentDto.CommentRequest commentRequest) {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("댓글 수정 권한이 없습니다.");
        }

        UserEntity user = comment.getUser();
        userService.checkUserLimit(user);

        comment.setContent(commentRequest.getContent());
        comment.setUpdatedAt(LocalDateTime.now());

        commentRepository.save(comment);
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(String userId, int commentId) {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("댓글 삭제 권한이 없습니다.");
        }

        commentRepository.delete(comment);
    }
}
