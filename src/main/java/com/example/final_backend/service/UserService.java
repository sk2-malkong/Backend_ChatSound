package com.example.final_backend.service;

import com.example.final_backend.dto.UpdateProfileDto;
import com.example.final_backend.entity.UserEntity;
import com.example.final_backend.repository.AuthRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;
import java.io.File;
import java.io.IOException;

/**
 * 사용자 관련 로직 처리 서비스
 */

@Service
@RequiredArgsConstructor
public class UserService {
    private final AuthRepository authRepository;

    // 프로필 정보 조회
    public UserEntity getProfile(String userId) {
        return authRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));
    }

    // 프로필 정보 수정
    @Transactional
    public void updateProfile(String userId, UpdateProfileDto dto) {
        UserEntity user = authRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        user.setUsername(dto.getUsername());
        user.setProfileImage(dto.getProfileImage());
        user.setUpdatedAt(LocalDateTime.now());
    }

    // 이미지 업로드
    public String uploadProfileImage(String userId, MultipartFile file) throws IOException {
        UserEntity user = authRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String uploadDir = System.getProperty("user.dir") + "/upload/profile";
        String filename = user.getUsername() + "_" + file.getOriginalFilename(); // ✅ username → user.getUsername()
        File savedFile = new File(uploadDir, filename);

        if (!savedFile.getParentFile().exists()) {
            savedFile.getParentFile().mkdirs(); // ✅ 디렉토리 생성
        }

        file.transferTo(savedFile); // ✅ 예외 발생 가능하므로 throws IOException

        String imageUrl = "http://localhost:8080/images/profile/" + filename;  // 배포 시 도메인으로 변경 필요
        user.setProfileImage(imageUrl);
        user.setUpdatedAt(LocalDateTime.now());

        authRepository.save(user);
        return imageUrl; // ✅ URL 반환
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
