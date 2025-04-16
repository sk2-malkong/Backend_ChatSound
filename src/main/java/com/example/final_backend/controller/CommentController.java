package com.example.final_backend.controller;

import com.example.final_backend.dto.CommentDto;
import com.example.final_backend.security.CustomUserDetails;
import com.example.final_backend.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comment")
public class CommentController {
    private final CommentService commentService;

    // 댓글 목록 조회
    @GetMapping("/{postId}")
    public ResponseEntity<List<CommentDto.Response>> getCommentsByPostId(@PathVariable int postId) {
        return ResponseEntity.ok(commentService.getCommentsByPostId(postId));
    }

    // 댓글 작성
    @PostMapping("/{postId}")
    public ResponseEntity<Integer> createComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable int postId,
            @RequestBody CommentDto.Request request
    ) {
        int commentId = commentService.createComment(userDetails.getId(), postId, request);
        return ResponseEntity.ok(commentId);
    }

    // 댓글 수정
    @PutMapping("/{commentId}")
    public ResponseEntity<String> updateComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable int commentId,
            @RequestBody CommentDto.Request request
    ) {
        commentService.updateComment(userDetails.getId(), commentId, request);
        return ResponseEntity.ok("댓글이 성공적으로 수정되었습니다.");
    }

    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<String> deleteComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable int commentId
    ) {
        commentService.deleteComment(userDetails.getId(), commentId);
        return ResponseEntity.ok("댓글이 성공적으로 삭제되었습니다.");
    }
}