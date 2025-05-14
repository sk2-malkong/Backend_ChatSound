package com.example.final_backend.controller;

import com.example.final_backend.dto.PostDto;
import com.example.final_backend.security.CustomUserDetails;
import com.example.final_backend.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * /api/post로 시작하는 게시글 관련 REST API 제공하는 컨트롤러
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/post")
public class PostController {
    private final PostService postService;

    // 게시물 목록 조회 (페이지네이션)
    @Operation(summary = "전체 게시물 조회", description = "전체 게시물을 조회할 수 있습니다.")
    @ApiResponse(responseCode = "200", description = "게시글 목록이 성공적으로 반환됩니다.")
    @GetMapping("/list")
    public ResponseEntity<Page<PostDto.CheckPostResponse>> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction
    ) {
        Sort.Direction sortDirection = direction.equalsIgnoreCase("ASC") ?
                Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        Page<PostDto.CheckPostResponse> posts = postService.getPostsWithPaging(pageable);

        return ResponseEntity.ok(posts);
    }

    // 게시물 상세 조회
    @Operation(summary = "상세 게시물 조회", description = "게시물 ID를 통해 해당 게시물의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "지정한 ID에 해당하는 게시물의 상세 정보가 반환됩니다.")
    @GetMapping("/{postId}")
    public ResponseEntity<PostDto.CheckDetailsResponse> getPost(@PathVariable int postId, @RequestParam(defaultValue = "true") boolean increaseView) {
        return ResponseEntity.ok(postService.getPostById(postId, increaseView));
    }

    // 게시물 생성
    @Operation(summary = "게시물 생성", description = "요청한 게시글 정보를 기반으로 새 게시물을 생성합니다.")
    @ApiResponse(responseCode = "200", description = "게시물이 성공적으로 생성되었습니다.")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/create")
    public ResponseEntity<PostDto.WritePostResponse> createPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody PostDto.PostRequest postRequest
    ) {
        PostDto.WritePostResponse writePostResponse = postService.createPost(userDetails.getId(), postRequest);
        return ResponseEntity.ok(writePostResponse);
    }

    // 게시물 수정
    @Operation(summary = "게시물 수정", description = "게시물 ID와 수정할 내용을 받아 해당 게시물을 수정합니다.")
    @ApiResponse(responseCode = "200", description = "게시물이 성공적으로 수정되었습니다.")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/update/{postId}")
    public ResponseEntity<PostDto.WritePostResponse> updatePost(
            @AuthenticationPrincipal CustomUserDetails userDetails, // 여기가 null일 수도 있음
            @PathVariable int postId,
            @RequestBody PostDto.PostRequest postRequest
    ) {
        PostDto.WritePostResponse writePostResponse = postService.updatePost(userDetails.getId(), postId, postRequest);
        return ResponseEntity.ok(writePostResponse);
    }

    // 게시물 삭제
    @Operation(summary = "게시물 삭제", description = "게시물 ID를 받아 해당 게시물을 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "게시물이 성공적으로 삭제되었습니다.")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/delete/{postId}")
    public ResponseEntity<String> deletePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable int postId
    ) {
        postService.deletePost(userDetails.getId(), postId);
        return ResponseEntity.ok("게시물이 성공적으로 삭제되었습니다.");
    }

    //내 게시글 조회
    @Operation(summary = "내 게시물 조회", description = "로그인한 사용자가 작성한 게시글 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "사용자가 작성한 게시글 목록이 성공적으로 반환됩니다.")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/my")
    public ResponseEntity<Page<PostDto.CheckPostResponse>> getMyPosts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction
    ) {
        Sort.Direction sortDirection = direction.equalsIgnoreCase("ASC") ?
                Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        Page<PostDto.CheckPostResponse> posts = postService.getMyPosts(userDetails.getId(), pageable);

        return ResponseEntity.ok(posts);
    }
}