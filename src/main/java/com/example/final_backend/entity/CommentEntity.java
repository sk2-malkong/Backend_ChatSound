package com.example.final_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "comments")
@Data
public class CommentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int commentId;

    // 사용자 식별 번호
    @ManyToOne
    @JoinColumn(name="users")
    private UserEntity userId;

    // 게시물 식별 번호
    @ManyToOne
    @JoinColumn(name = "posts")
    private PostEntity postId;

    // 댓글 내용
    @Column(nullable = false, length = 1000)
    private String content;

    // 댓글 생성 일자
    private LocalDateTime createdAt;

    // 댓글 수정 일자
    private LocalDateTime updatedAt;
}
