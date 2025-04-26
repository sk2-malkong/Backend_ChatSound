package com.example.final_backend.service;

import com.example.final_backend.entity.BadwordLogEntity;
import com.example.final_backend.repository.AuthRepository;
import com.example.final_backend.dto.PostDto;
import com.example.final_backend.entity.PostEntity;
import com.example.final_backend.entity.UserEntity;
import com.example.final_backend.repository.BadwordLogRepository;
import com.example.final_backend.repository.PostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final AuthRepository authRepository;
    private final RestTemplate purgoRestTemplate;
    private final BadwordLogRepository badwordLogRepository;
    private final UserService userService;

    @Value("${proxy.server.url}")
    private String gatewayUrl;

    // 욕설 필터링 함수 (FastAPI 호출)
    private String getFilteredText(String text, UserEntity user, PostEntity post) {
        try {
            System.out.println("📤 FastAPI로 전송할 텍스트 (게시글): " + text);

            Map<String, String> body = new HashMap<>();
            body.put("text", text);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = purgoRestTemplate.postForEntity(gatewayUrl, entity, Map.class);


            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> result = response.getBody();

                System.out.println("📦 FastAPI 응답 전체: " + result);

                // final_decision 기준으로 판단
                Object decision = result.get("final_decision");
                Boolean isAbusive = decision != null && decision.toString().equals("1");

                // result 객체 안의 rewritten_text 추출
                Map<String, Object> resultInner = (Map<String, Object>) result.get("result");
                String rewritten = resultInner != null ? (String) resultInner.get("rewritten_text") : text;

                System.out.println("욕설 여부: " + isAbusive);
                System.out.println("대체 문장: " + rewritten);

                if (Boolean.TRUE.equals(isAbusive)) {
                    BadwordLogEntity log = new BadwordLogEntity();
                    log.setUser(user);
                    log.setPost(post);
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



    // 게시글 작성
    @Transactional
    public PostDto.Response createPost(String userId, PostDto.Request request) {
        UserEntity user = authRepository.findById(userId).orElseThrow();

        // 제한 여부 확인
        userService.checkUserLimit(user);

        PostEntity post = new PostEntity();
        post.setUser(user);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        post.setCount(0);
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());

        // 우선 저장 후 postId 생성
        PostEntity saved = postRepository.save(post);

        // 제목과 내용 욕설 감지 및 로그 저장
        saved.setTitle(getFilteredText(request.getTitle(), user, saved));
        saved.setContent(getFilteredText(request.getContent(), user, saved));
        return mapToDto(postRepository.save(saved));
    }

    // 게시글 목록 조회
    public List<PostDto.Response> getAllPosts() {
        List<PostEntity> posts = postRepository.findAllByOrderByCreatedAtDesc();
        return posts.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // 게시글 상세 조회
    @Transactional
    public PostDto.Response getPostById(int postId, boolean increaseView) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: " + postId));


        // 조회수 증가
        if (increaseView) {
            post.setCount(post.getCount() + 1);
            postRepository.save(post);
        }

        return mapToDto(post);
    }

    // 사용자별 게시글 조회
    public List<PostDto.Response> getPostsByUserId(String userId) {
        UserEntity user = authRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + userId));

        List<PostEntity> posts = postRepository.findByUserId(user);
        return posts.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // 게시글 수정
    @Transactional
    public PostDto.Response updatePost(String userId, int postId, PostDto.Request request) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: " + postId));

        // 작성자 확인
        if (!post.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("게시글 수정 권한이 없습니다.");
        }

        UserEntity user = post.getUser();

        // 제한 여부 확인
        userService.checkUserLimit(user);

        post.setTitle(getFilteredText(request.getTitle(), user, post));
        post.setContent(getFilteredText(request.getContent(), user, post));
        post.setUpdatedAt(LocalDateTime.now());

        PostEntity updatedPost = postRepository.save(post);
        return mapToDto(updatedPost);
    }

    // 게시글 삭제
    @Transactional
    public void deletePost(String userId, int postId) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: " + postId));

        // 작성자 확인
        if (!post.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("게시글 삭제 권한이 없습니다.");
        }

        postRepository.delete(post);
    }

    // Entity를 DTO로 변환
    private PostDto.Response mapToDto(PostEntity post) {
        return PostDto.Response.builder()
                .postId(post.getPostId())
                .userId(post.getUser().getId())
                .username(post.getUser().getUsername())
                .title(post.getTitle())
                .content(post.getContent())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .count(post.getCount())
                .build();
    }

    // 게시글 페이징 조회
    public Page<PostDto.Response> getPostsWithPaging(Pageable pageable) {
        return postRepository.findAll(pageable)
                .map(this::mapToDto);
    }

    // 게시글 검색
    public Page<PostDto.Response> searchPosts(String keyword, Pageable pageable) {
        return postRepository.findByTitleOrContentContaining(keyword, pageable)
                .map(this::mapToDto);
    }

    // 내 게시글 조회
    public Page<PostDto.Response> getMyPosts(String userId, Pageable pageable) {
        return postRepository.findByUserId_Id(userId, pageable)
                .map(this::mapToDto);
    }
}
