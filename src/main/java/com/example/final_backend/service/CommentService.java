package com.example.final_backend.service;

import com.example.final_backend.dto.CommentDto;
import com.example.final_backend.entity.*;
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
    private final ServerToProxyJwtService serverToProxyJwtService; // 서버 간 JWT 생성을 위한 서비스 추가

    @Value("${proxy.server.url}")
    private String gatewayUrl;

    @Value("${PURGO_CLIENT_API_KEY}")
    private String clientApiKey;

    // 욕설 필터링 + 로그 저장 로직 (FastAPI 프록시 호출)
    private String getFilteredText(String text, UserEntity user, PostEntity post, CommentEntity comment) {
        try {
            Map<String, String> body = new HashMap<>();
            body.put("text", text);

            // JWT 생성
            String jsonBody = serverToProxyJwtService.createJsonBody(body);
            String serverJwt = serverToProxyJwtService.generateTokenFromJson(jsonBody);

            // 헤더 설정
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
                    if (comment != null) { // comment가 있을 때만 로그 저장
                        BadwordLogEntity log = new BadwordLogEntity();
                        log.setUser(user);
                        log.setPost(post);
                        log.setComment(comment);
                        log.setOriginalWord(text);
                        log.setFilteredWord(rewritten);
                        log.setCreatedAt(LocalDateTime.now());
                        badwordLogRepository.save(log);
                    }

                    userService.applyPenalty(user.getUserId()); // 패널티는 항상 적용
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

    // 댓글 작성
    @Transactional
    public CommentDto.CommentResponse createComment(String userId, int postId, CommentDto.CommentRequest commentRequest) {
        UserEntity user = authRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 사용자 제한 여부 확인
        userService.checkUserLimit(user);

        // 댓글 엔티티 생성 및 저장 (commentId 확보용)
        CommentEntity comment = new CommentEntity();
        comment.setUser(user);
        comment.setPost(post);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        comment.setContent("임시"); // placeholder
        comment = commentRepository.save(comment); // save() 후 ID 생성됨

        // 3욕설 필터링 + 로그 저장 (이제 comment를 넘길 수 있음)
        String refined = getFilteredText(commentRequest.getContent(), user, post, comment);
        comment.setContent(refined);

        // 최종 저장
        commentRepository.save(comment);

        /*
        프론트에서 사용자 횟수 제한 시 화면에서도 제한을 하기 위해 필요한 데이터 값
        penaltyCount, endDate 를 전달
         */

        // 제한 종료시간 추출
        LocalDateTime endDate = null;
        if (user.getLimits() != null) {
            endDate = user.getLimits().getEndDate();
        }

        // 패널티 횟수 추출
        int penaltyCount = 0;
        if (user.getPenaltyCount() != null) {
            penaltyCount = user.getPenaltyCount().getPenaltyCount();
        }

        return CommentDto.CommentResponse.builder()
                .commentId(comment.getCommentId())
                .userId(comment.getUser().getId())
                .username(comment.getUser().getUsername())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                // 프론트 전달용 데이터
                .penaltyCount(penaltyCount)
                .endDate(endDate)
                .build();
    }


    // 댓글 수정
    @Transactional
    public CommentDto.CommentResponse updateComment(String userId, int commentId, CommentDto.CommentRequest commentRequest) {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("댓글 수정 권한이 없습니다.");
        }

        UserEntity user = comment.getUser();
        PostEntity post = comment.getPost();

        // 사용자 제한 여부 확인
        userService.checkUserLimit(user);

        // 욕설 필터링 + 로그 저장 + 패널티 적용
        String refined = getFilteredText(commentRequest.getContent(), user, post, comment);

        // 내용 반영
        comment.setContent(refined);
        comment.setUpdatedAt(LocalDateTime.now());

        commentRepository.save(comment);

        // endDate 추출
        LocalDateTime endDate = null;
        if (user.getLimits() != null) {
            endDate = user.getLimits().getEndDate();
        }

        // penaltyCount 추출
        int penaltyCount = 0;
        if (user.getPenaltyCount() != null) {
            penaltyCount = user.getPenaltyCount().getPenaltyCount();
        }

        return CommentDto.CommentResponse.builder()
                .commentId(comment.getCommentId())
                .userId(comment.getUser().getId())
                .username(comment.getUser().getUsername())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                // 프론트 전달용 데이터
                .penaltyCount(penaltyCount)
                .endDate(endDate)
                .build();
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
