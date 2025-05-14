package com.example.final_backend.repository;

import com.example.final_backend.entity.PostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 게시글 관리 Repository
 */

@Repository
public interface PostRepository extends JpaRepository<PostEntity, Integer> {

    // 내 게시글 (로그인한 사용자의 ID 기준)
    Page<PostEntity> findByUserId_Id(String userId, Pageable pageable);

    // orElseThrow() 사용 시 타입 오류 없이 PostEntity로 받을 수 있음
    Optional<PostEntity> findByPostId(int postId);

    // 게시글(PostEntity)과 연결된 댓글(CommentEntity)의 개수를 함께 조회하여 페이지로 반환
    @Query("SELECT p, COUNT(c) as commentCount FROM PostEntity p LEFT JOIN p.comment c GROUP BY p")
    Page<Object[]> findAllWithCommentCount(Pageable pageable);

    // 게시글 + 댓글 수 키워드 기반 검색 JPQL
    @Query("""
    SELECT p, COUNT(c) as commentCount
    FROM PostEntity p
    LEFT JOIN CommentEntity c ON c.post.postId = p.postId
    WHERE p.title LIKE %:keyword% OR p.content LIKE %:keyword%
    GROUP BY p
    """)
    Page<Object[]> findAllWithCommentCountByKeyword(@Param("keyword") String keyword, Pageable pageable);

}
