package com.example.final_backend.repository;

import com.example.final_backend.entity.UserLimitsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserLimitsRepository extends JpaRepository<UserLimitsEntity, Integer> {
    Optional<UserLimitsEntity> findByUserId(int userId);
}