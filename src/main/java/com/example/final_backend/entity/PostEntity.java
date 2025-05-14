package com.example.final_backend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 게시물 Entity
 */

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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    @JsonBackReference
    private UserEntity user;

    // 게시물 제목
    private String title;

    // 게시물 내용
    private String content;

    // 게시물 생성 일자
    private LocalDateTime createdAt;

    // 게시물 수정 일자
    private LocalDateTime updatedAt;

    // 게시물 조회수
    private int count;

    // 댓글과 1대다 양방향 연결
    @JsonManagedReference
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private List<CommentEntity> comment;

    // 게시물 댓글 수
    @Transient // JPA가 DB 컬럼으로 인식하지 않음
    private int commentCount;

    public static PostEntity create(UserEntity user, String title, String content) {
        PostEntity post = new PostEntity();
        post.setUser(user);
        post.setTitle(title);
        post.setContent(content);
        post.setCount(0);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        return post;
    }
}
