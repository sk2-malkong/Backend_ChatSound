package com.example.final_backend.service;

import com.example.final_backend.dto.CommentDto;
import com.example.final_backend.entity.CommentEntity;
import com.example.final_backend.entity.PostEntity;
import com.example.final_backend.entity.UserEntity;
import com.example.final_backend.repository.AuthRepository;
import com.example.final_backend.repository.CommentRepository;
import com.example.final_backend.repository.PostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final AuthRepository authRepository;

    // 게시글에 달린 댓글 목록 조회
    public List<CommentDto.Response> getCommentsByPostId(int postId) {
        PostEntity post = (PostEntity) postRepository.findByPostId(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        List<CommentEntity> comments = commentRepository.findByPost(post);
        List<CommentDto.Response> response = new ArrayList<>();

        for (CommentEntity comment : comments) {
            CommentDto.Response dto = new CommentDto.Response();
            dto.setCommentId(comment.getCommentId());
            dto.setUserId(comment.getUser().getId());
            dto.setUsername(comment.getUser().getUsername());
            dto.setContent(comment.getContent());
            dto.setCreatedAt(comment.getCreatedAt());
            dto.setUpdatedAt(comment.getUpdatedAt());

            response.add(dto);
        }

        return response;
    }

    // 댓글 작성
    @Transactional
    public int createComment(String userId, int postId, CommentDto.Request request) {
        UserEntity user = authRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        PostEntity post = (PostEntity) postRepository.findByPostId(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        CommentEntity comment = new CommentEntity();
        comment.setUser(user);
        comment.setPost(post);
        comment.setContent(request.getContent());
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());

        CommentEntity savedComment = commentRepository.save(comment);
        return savedComment.getCommentId();
    }

    // 댓글 수정
    @Transactional
    public void updateComment(String userId, int commentId, CommentDto.Request request) {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        // 작성자 확인
        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("댓글 수정 권한이 없습니다.");
        }

        comment.setContent(request.getContent());
        comment.setUpdatedAt(LocalDateTime.now());

        commentRepository.save(comment);
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(String userId, int commentId) {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        // 작성자 확인
        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("댓글 삭제 권한이 없습니다.");
        }

        commentRepository.delete(comment);
    }
}