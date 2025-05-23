package com.example.final_backend.service;

import com.example.final_backend.dto.ProfileDto;
import com.example.final_backend.entity.UserEntity;
import com.example.final_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.io.File;
import java.io.IOException;

/**
 * 마이페이지 관련 로직 처리 서비스
 */

@Service
@RequiredArgsConstructor
public class MypageService {
    private final UserRepository userRepository;

    // 프로필 이미지 URL
    @Value("${profile.image-url}")
    private String profileImageUrl;

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

    // 이미지 업로드
    public String uploadProfileImage(String userId, MultipartFile file) throws IOException {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String uploadDir = System.getProperty("user.dir") + "/upload/profile";
        String filename = user.getUsername() + "_" + file.getOriginalFilename(); //  username → user.getUsername()
        File savedFile = new File(uploadDir, filename);

        if (!savedFile.getParentFile().exists()) {
            savedFile.getParentFile().mkdirs(); // 디렉토리 생성
        }

        file.transferTo(savedFile); // 예외 발생 가능하므로 throws IOException

        String imageUrl = profileImageUrl + filename;  // 배포 시 도메인으로 변경 필요
        user.setProfileImage(imageUrl);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
        return imageUrl; // URL 반환
    }

    // 회원 탈퇴
    @Transactional
    public void deleteUser(String userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        userRepository.delete(user);
    }
}
