package com.example.final_backend.controller;

import com.example.final_backend.dto.PostDto;
import com.example.final_backend.security.CustomUserDetails;
import com.example.final_backend.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/post")
public class PostController {
    private final PostService postService;

    // 게시물 목록 조회 (페이지네이션)
    @GetMapping("/list")
    public ResponseEntity<Page<PostDto.Response>> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction
    ) {
        Sort.Direction sortDirection = direction.equalsIgnoreCase("ASC") ?
                Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        Page<PostDto.Response> posts = postService.getPostsWithPaging(pageable);

        return ResponseEntity.ok(posts);
    }

    // 게시물 상세 조회
    @GetMapping("/{postId}")
    public ResponseEntity<PostDto.Response> getPost(@PathVariable int postId) {
        return ResponseEntity.ok(postService.getPostById(postId));
    }

    // 게시물 생성
    @PostMapping("/create")
    public ResponseEntity<PostDto.Response> createPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody PostDto.Request request
    ) {
        PostDto.Response response = postService.createPost(userDetails.getId(), request);
        return ResponseEntity.ok(response);
    }

    // 게시물 수정
    @PutMapping("/update/{postId}")
    public ResponseEntity<PostDto.Response> updatePost(
            @AuthenticationPrincipal CustomUserDetails userDetails, // 여기가 null일 수도 있음
            @PathVariable int postId,
            @RequestBody PostDto.Request request
    ) {
        System.out.println("userDetails = " + userDetails); // null인지 확인
        PostDto.Response response = postService.updatePost(userDetails.getId(), postId, request);
        return ResponseEntity.ok(response);
    }

    // 게시물 삭제
    @DeleteMapping("/delete/{postId}")
    public ResponseEntity<String> deletePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable int postId
    ) {
        postService.deletePost(userDetails.getId(), postId);
        return ResponseEntity.ok("게시물이 성공적으로 삭제되었습니다.");
    }

    //내 게시글 조회
    @GetMapping("/my")
    public ResponseEntity<Page<PostDto.Response>> getMyPosts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction
    ) {
        Sort.Direction sortDirection = direction.equalsIgnoreCase("ASC") ?
                Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        Page<PostDto.Response> posts = postService.getMyPosts(userDetails.getId(), pageable);

        return ResponseEntity.ok(posts);
    }
}