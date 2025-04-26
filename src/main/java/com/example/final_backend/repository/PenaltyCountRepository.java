package com.example.final_backend.repository;

import com.example.final_backend.entity.PenaltyCountEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PenaltyCountRepository extends JpaRepository<PenaltyCountEntity, Integer> {
    Optional<PenaltyCountEntity> findByUserId(int userId);
}
