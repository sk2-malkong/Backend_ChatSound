package com.example.final_backend.service;


import com.example.final_backend.repository.AuthRepository;
import com.example.final_backend.dto.PostDto;
import com.example.final_backend.entity.PostEntity;
import com.example.final_backend.entity.UserEntity;
import com.example.final_backend.repository.PostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final AuthRepository authRepository;

    // 게시글 작성
    @Transactional
    public PostDto.Response createPost(String userId, PostDto.Request request) {
        UserEntity user = authRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + userId));

        PostEntity post = new PostEntity();
        post.setUser(user);
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        post.setCount(0);

        PostEntity savedPost = postRepository.save(post);

        return mapToDto(savedPost);
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
    public PostDto.Response getPostById(int postId) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: " + postId));

        // 조회수 증가
        post.setCount(post.getCount() + 1);
        postRepository.save(post);

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

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
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

    //내 게시글 조회
    public Page<PostDto.Response> getMyPosts(String userId, Pageable pageable) {
        return postRepository.findByUserId_Id(userId, pageable)
                .map(this::mapToDto);
    }


}
