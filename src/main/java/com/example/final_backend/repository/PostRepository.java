package com.example.final_backend.repository;

import com.example.final_backend.entity.PostEntity;
import com.example.final_backend.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<PostEntity, Integer> {
    List<PostEntity> findByUserId(UserEntity userId);
    List<PostEntity> findAllByOrderByCreatedAtDesc();
    // 제목 또는 내용으로 검색
    @Query("SELECT p FROM PostEntity p WHERE p.title LIKE %:keyword% OR p.content LIKE %:keyword%")
    Page<PostEntity> findByTitleOrContentContaining(@Param("keyword") String keyword, Pageable pageable);
    // 내 게시글 (로그인한 사용자의 ID 기준)
    Page<PostEntity> findByUserId_Id(String userId, Pageable pageable);

    // 특정 사용자의 게시물 목록
    List<PostEntity> findByUserId_Id(String userId);

    Optional<Object> findByPostId(int postId);
}
