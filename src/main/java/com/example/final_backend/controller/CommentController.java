package com.example.final_backend.controller;

import com.example.final_backend.dto.CommentDto;
import com.example.final_backend.security.CustomUserDetails;
import com.example.final_backend.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * /api/comment로 시작하는 댓글 관련 REST API 제공하는 컨트롤러
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comment")
public class CommentController {
    private final CommentService commentService;

    // 댓글 목록 조회
    @Operation(summary = "댓글 목록 조회", description = "특정 게시글에 작성된 모든 댓글을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "댓글 목록이 성공적으로 반환됩니다.")
    @GetMapping("/{postId}")
    public ResponseEntity<List<CommentDto.CheckCommentResponse>> getCommentsByPostId(@PathVariable int postId) {
        return ResponseEntity.ok(commentService.getCommentsByPostId(postId));
    }

    // 댓글 작성
    @Operation(summary = "댓글 작성", description = "특정 게시글에 댓글을 작성합니다.")
    @ApiResponse(responseCode = "200", description = "댓글이 성공적으로 작성되었습니다.")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{postId}")
    public ResponseEntity<CommentDto.WriteCommentResponse> createComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable int postId,
            @RequestBody CommentDto.CommentRequest commentRequest
    ) {
        CommentDto.WriteCommentResponse response = commentService.createComment(userDetails.getId(), postId, commentRequest);
        return ResponseEntity.ok(response);
    }

    // 댓글 수정
    @Operation(summary = "댓글 수정", description = "작성한 댓글의 내용을 수정합니다.")
    @ApiResponse(responseCode = "200", description = "댓글이 성공적으로 수정되었습니다.")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{commentId}")
    public ResponseEntity<CommentDto.WriteCommentResponse> updateComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable int commentId,
            @RequestBody CommentDto.CommentRequest commentRequest
    ) {
        CommentDto.WriteCommentResponse response = commentService.updateComment(userDetails.getId(), commentId, commentRequest);
        return ResponseEntity.ok(response);
    }

    // 댓글 삭제
    @Operation(summary = "댓글 삭제", description = "작성한 댓글을 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "댓글이 성공적으로 삭제되었습니다.")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<String> deleteComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable int commentId
    ) {
        commentService.deleteComment(userDetails.getId(), commentId);
        return ResponseEntity.ok("댓글이 성공적으로 삭제되었습니다.");
    }
}