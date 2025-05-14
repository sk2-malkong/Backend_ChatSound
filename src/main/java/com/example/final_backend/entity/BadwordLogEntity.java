package com.example.final_backend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 사용자 비속어 사용 확인 로그 Entity
 */

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name="badwordLogs")
@Data
public class BadwordLogEntity {
    // 비속어 사용 로그 식별 번호
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int logId;

    // 사용자 식별 번호
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference
    @JoinColumn(name = "userId")
    private UserEntity user;

    // 게시물 식별 번호
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "postId")
    private PostEntity post;

    // 댓글 식별 번호
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commentId")
    private CommentEntity comment;

    // 사용한 비속어
    private String originalWord;

    // 대체어
    private String filteredWord;

    // 비속어 사용 일자
    private LocalDateTime createdAt;

    public static BadwordLogEntity of(UserEntity user, PostEntity post, CommentEntity comment, String original, String filtered) {
        BadwordLogEntity log = new BadwordLogEntity();
        log.setUser(user);
        log.setPost(post);
        log.setOriginalWord(original);
        log.setFilteredWord(filtered);
        log.setCreatedAt(LocalDateTime.now());
        if (comment != null) {
            log.setComment(comment);
        }
        return log;
    }
}
