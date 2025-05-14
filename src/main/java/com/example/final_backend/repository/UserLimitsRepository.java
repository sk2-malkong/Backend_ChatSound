package com.example.final_backend.repository;

import com.example.final_backend.entity.UserLimitsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 사용자 이용 제한 관리 Repository
 */

@Repository
public interface UserLimitsRepository extends JpaRepository<UserLimitsEntity, Integer> {
    // 사용자 id 찾기
    Optional<UserLimitsEntity> findByUserId(int userId);
}