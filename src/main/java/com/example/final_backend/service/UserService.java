package com.example.final_backend.service;

import com.example.final_backend.dto.AuthDto;
import com.example.final_backend.entity.UserEntity;
import com.example.final_backend.repository.AuthRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;

/**
 * 사용자 관련 로직 처리 서비스
 */

@Service
@RequiredArgsConstructor
public class UserService {
    private final AuthRepository authRepository;

    // ✅ 프로필 정보 조회
    public UserEntity getProfile(String userId) {
        return authRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));
    }

    // ✅ 프로필 정보 수정
    @Transactional
    public void updateProfile(String userId, AuthDto dto) {
        UserEntity user = authRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        user.setUsername(dto.getUsername());
        user.setProfileImage(dto.getProfileImage());
        user.setUpdatedAt(LocalDateTime.now());
    }

    // 이미지 업로드
    public String uploadProfileImage(String userId, MultipartFile file) {
        UserEntity user = authRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 예시: 실제 저장 로직 대신 파일명을 저장
        String filename = file.getOriginalFilename(); // or S3에 업로드 시 URL
        user.setProfileImage(filename);
        user.setUpdatedAt(LocalDateTime.now());

        authRepository.save(user);
        return filename;
    }

    // 회원 탈퇴
    @Transactional
    public void deleteUser(String userId) {
        UserEntity user = authRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        authRepository.delete(user);
    }

    public int getPenaltyCount(String userId) {
        UserEntity user = authRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return user.getPenaltyCount() != null ? user.getPenaltyCount().getPenaltyCount() : 0;
    }

    public Map<String, Object> getLimitInfo(String userId) {
        UserEntity user = authRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Map<String, Object> map = new HashMap<>();
        if (user.getLimits() != null) {
            map.put("startDate", user.getLimits().getStartDate());
            map.put("endDate", user.getLimits().getEndDate());
            map.put("isActive", user.getLimits().getIsActive());
        } else {
            map.put("message", "제한 기록 없음");
        }

        return map;
    }

}
