package com.example.final_backend.service;

import com.example.final_backend.dto.ProfileDto;
import com.example.final_backend.entity.UserEntity;
import com.example.final_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * 마이페이지 관련 로직 처리 서비스
 */

@Service
@RequiredArgsConstructor
public class MypageService {
    private final UserRepository userRepository;

    // 프로필 정보 조회
    public ProfileDto.UserProfile getProfile(String userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        return ProfileDto.UserProfile.of(user);
    }

    // 프로필 정보 수정
    @Transactional
    public void updateProfile(String userId, ProfileDto.UpdateProfile updateProfileDto) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        updateProfileDto.UpdateProfile(user);
    }

    // 회원 탈퇴
    @Transactional
    public void deleteUser(String userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        userRepository.delete(user);
    }
}
