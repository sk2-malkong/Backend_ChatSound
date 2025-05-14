package com.example.final_backend.factory;

import com.example.final_backend.dto.AuthDto;
import com.example.final_backend.entity.PenaltyCountEntity;
import com.example.final_backend.entity.UserEntity;
import com.example.final_backend.entity.UserLimitsEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 회원가입 시 기본 설정이 적용된 UserEntity를 생성하는 팩토리 클래스
 */
public class UserFactory {

    // 회원가입 시 기본 Penalty, Limits 설정을 포함한 UserEntity 생성
    public static UserEntity createWithDefaults(AuthDto dto, String encodedPw) {
        UserEntity user = new UserEntity();
        user.setId(dto.getId());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPw(encodedPw);
        user.setProfileImage(dto.getProfileImage());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // 기본 PenaltyCountEntity 설정
        PenaltyCountEntity penalty = new PenaltyCountEntity();
        penalty.setUser(user);
        penalty.setPenaltyCount(0);
        penalty.setLastUpdate(LocalDate.now());
        penalty.setUserId(user.getUserId()); // @MapsId 설정 주의

        user.setPenaltyCount(penalty);

        // 기본 UserLimitsEntity 설정
        UserLimitsEntity limits = new UserLimitsEntity();
        limits.setUser(user);
        limits.setUserId(user.getUserId()); // @MapsId 설정 주의
        limits.setIsActive(true);
        limits.setStartDate(null);
        limits.setEndDate(null);

        user.setLimits(limits);

        return user;
    }
}
