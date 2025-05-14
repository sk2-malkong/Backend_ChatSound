package com.example.final_backend.service;

import com.example.final_backend.dto.CommentDto;
import com.example.final_backend.entity.*;
import com.example.final_backend.repository.UserRepository;
import com.example.final_backend.repository.CommentRepository;
import com.example.final_backend.repository.PostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CheckBadwordService checkBadwordService;
    private final UserPenaltyService userPenaltyService;

    // 댓글 조회
    public List<CommentDto.CheckCommentResponse> getCommentsByPostId(int postId) {
        PostEntity post = postRepository.findByPostId(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        return commentRepository.findByPost(post).stream()
                .map(CommentDto.CheckCommentResponse::of)
                .collect(Collectors.toList());
    }

    // 댓글 작성
    @Transactional
    public CommentDto.WriteCommentResponse createComment(String userId, int postId, CommentDto.CommentRequest commentRequestDto) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 사용자 제한 여부 확인
        userPenaltyService.checkUserLimit(user);

        // 댓글 엔티티 생성 및 저장 (commentId 확보용)
        CommentEntity comment = CommentEntity.create(user, post);
        comment = commentRepository.save(comment);

        // 욕설 필터링 + 로그 저장
        String refined = checkBadwordService.getFilteredText(commentRequestDto.getContent(), user, post, comment);
        comment.setContent(refined);

        // 최종 저장
        commentRepository.save(comment);

        return CommentDto.WriteCommentResponse.of(comment, user);
    }


    // 댓글 수정
    @Transactional
    public CommentDto.WriteCommentResponse updateComment(String userId, int commentId, CommentDto.CommentRequest commentRequestDto) {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("댓글 수정 권한이 없습니다.");
        }

        UserEntity user = comment.getUser();
        PostEntity post = comment.getPost();

        // 사용자 제한 여부 확인
        userPenaltyService.checkUserLimit(user);

        // 욕설 필터링 + 로그 저장 + 패널티 적용
        String refined = checkBadwordService.getFilteredText(commentRequestDto.getContent(), user, post, comment);

        // 내용 반영
        comment.setContent(refined);
        comment.setUpdatedAt(LocalDateTime.now());

        commentRepository.save(comment);

        return CommentDto.WriteCommentResponse.of(comment, user);
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
