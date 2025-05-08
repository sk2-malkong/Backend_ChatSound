package com.example.final_backend.repository;

import com.example.final_backend.entity.CommentEntity;
import com.example.final_backend.entity.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Integer> {
    List<CommentEntity> findByPost(PostEntity post);

    int countByPost(PostEntity updated); // 게시글에서 댓글 수 조회용
}