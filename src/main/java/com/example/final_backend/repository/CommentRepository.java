package com.example.final_backend.repository;

import com.example.final_backend.entity.CommentEntity;
import com.example.final_backend.entity.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 *  댓글 관리 Repository
 */

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Integer> {
    // 게시글 ID 찾기
    List<CommentEntity> findByPost(PostEntity post);
}