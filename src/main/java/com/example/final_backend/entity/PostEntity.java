package com.example.final_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name="posts")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class PostEntity {
    // 게시물 식별 번호
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int postId;

    // 사용자 식별 번호
    @ManyToOne
    @JoinColumn(name = "users")
    private UserEntity userId;

    // 게시물 제목
    @Column(length = 50)
    private String title;

    // 게시물 내용
    @Column(length = 1000)
    private String content;

    // 게시물 생성 일자
    private LocalDateTime createdAt;

    // 게시물 수정 일자
    private LocalDateTime updatedAt;

    // 게시물 이미지
    @Column()
    private String image;

    // 게시물 조회수
    private int count;

    // 댓글과 1대다 양방향 연결
    @OneToMany(mappedBy = "posts", cascade = CascadeType.ALL)
    private List<CommentEntity> comment;
}
