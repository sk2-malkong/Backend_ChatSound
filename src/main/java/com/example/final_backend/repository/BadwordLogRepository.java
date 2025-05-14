package com.example.final_backend.repository;

import com.example.final_backend.entity.BadwordLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 비속어 로그 관리 Repository
 */

@Repository
public interface BadwordLogRepository extends JpaRepository<BadwordLogEntity, Integer> {
}
