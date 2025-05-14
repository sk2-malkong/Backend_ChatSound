package com.example.final_backend.repository;

import com.example.final_backend.entity.PenaltyCountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 비속어 사용 횟수 관리 Repository
 */

@Repository
public interface PenaltyCountRepository extends JpaRepository<PenaltyCountEntity, Integer> {
    // 사용자 Id 찾기
    Optional<PenaltyCountEntity> findByUserId(int userId);
}
