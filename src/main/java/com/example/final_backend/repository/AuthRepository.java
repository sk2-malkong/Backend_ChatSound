package com.example.final_backend.repository;

import com.example.final_backend.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@EnableJpaRepositories(basePackages = "com.example.final_backend.repository")
public interface AuthRepository extends JpaRepository<UserEntity, Integer> {
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findById(String id);
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByUserId(int userId);
}
