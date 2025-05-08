package com.example.final_backend.service;

import com.example.final_backend.dto.UpdateProfileDto;
import com.example.final_backend.dto.UserProfileDto;
import com.example.final_backend.entity.PenaltyCountEntity;
import com.example.final_backend.entity.UserEntity;
import com.example.final_backend.entity.UserLimitsEntity;
import com.example.final_backend.repository.AuthRepository;
import com.example.final_backend.repository.PenaltyCountRepository;
import com.example.final_backend.repository.UserLimitsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

import java.time.LocalDate;
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
    private final PenaltyCountRepository penaltyCountRepository;
    private final UserLimitsRepository userLimitsRepository;

    // 프로필 정보 조회
    public UserProfileDto getProfileDto(String userId) {
        UserEntity user = authRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        UserProfileDto dto = new UserProfileDto();
        dto.setUserId(user.getUserId());
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setProfileImage(user.getProfileImage());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());

        return dto;
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
        String filename = user.getUsername() + "_" + file.getOriginalFilename(); //  username → user.getUsername()
        File savedFile = new File(uploadDir, filename);

        if (!savedFile.getParentFile().exists()) {
            savedFile.getParentFile().mkdirs(); // 디렉토리 생성
        }

        file.transferTo(savedFile); // 예외 발생 가능하므로 throws IOException

        String imageUrl = "http://localhost:8080/images/profile/" + filename;  // 배포 시 도메인으로 변경 필요
        user.setProfileImage(imageUrl);
        user.setUpdatedAt(LocalDateTime.now());

        authRepository.save(user);
        return imageUrl; // URL 반환
    }

    // 회원 탈퇴
    @Transactional
    public void deleteUser(String userId) {
        UserEntity user = authRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        authRepository.delete(user);
    }

    // 사용자 욕설 감지 횟수 조회
    public int getPenaltyCount(String userId) {
        UserEntity user = authRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return user.getPenaltyCount() != null ? user.getPenaltyCount().getPenaltyCount() : 0;
    }

    // 사용자 제한 정보 조회
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

        // 비속어 로그 추가
        if (user.getBadwordLogs() != null && !user.getBadwordLogs().isEmpty()) {
            List<Map<String, Object>> logs = user.getBadwordLogs().stream()
                    .map(log -> {
                        Map<String, Object> entry = new HashMap<>();
                        entry.put("originalWord", log.getOriginalWord());
                        entry.put("filteredWord", log.getFilteredWord());
                        entry.put("createdAt", log.getCreatedAt());
                        return entry;
                    })
                    .toList();
            map.put("badwordLogs", logs);
        } else {
            map.put("badwordLogs", List.of());
        }
        return map;
    }

    // 사용자 패널티 관리
    @Transactional
    public void applyPenalty(int userId) {
        UserEntity user = authRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        PenaltyCountEntity penalty = penaltyCountRepository.findByUserId(userId)
                .orElseGet(() -> {
                    PenaltyCountEntity newPenalty = new PenaltyCountEntity();
                    newPenalty.setUser(user);
                    newPenalty.setUserId(userId);
                    newPenalty.setPenaltyCount(0);
                    return newPenalty;
                });

        penalty.setPenaltyCount(penalty.getPenaltyCount() + 1);
        penalty.setLastUpdate(LocalDate.now());

        penaltyCountRepository.save(penalty); // 기존 엔티티 업데이트

        // 제한 처리
        if (penalty.getPenaltyCount() % 5 == 0) {
            UserLimitsEntity limits = userLimitsRepository.findByUserId(userId)
                    .orElseGet(() -> {
                        UserLimitsEntity newLimits = new UserLimitsEntity();
                        newLimits.setUser(user);
                        newLimits.setUserId(userId);
                        return newLimits;
                    });

            LocalDateTime now = LocalDateTime.now();

            limits.setIsActive(false);
            limits.setStartDate(LocalDateTime.now());
            //limits.setEndDate(LocalDateTime.now().plusHours(24));   // 24시간 제한
            limits.setEndDate(now.plusMinutes(3));    // 코드 확인용 5분


            userLimitsRepository.save(limits);
        }
    }

    // 사용자 제한 상태 확인
    public void checkUserLimit(UserEntity user) {
        UserLimitsEntity limits = user.getLimits();

        if (limits != null && Boolean.FALSE.equals(limits.getIsActive())) {
            LocalDateTime now = LocalDateTime.now();

            // 제한 시간이 지났으면 자동 해제
            if (limits.getEndDate() != null && now.isAfter(limits.getEndDate())) {
                limits.setIsActive(true); // 제한 해제
                limits.setStartDate(null);
                limits.setEndDate(null);

                userLimitsRepository.save(limits); // DB 반영
                System.out.println("24시간 제한 자동 해제됨");
                return; // 복구되었으니 제한 아님
            }

            // 아직 제한 중이면 차단
            throw new IllegalStateException("욕설 사용 5회로 24시간 동안 게시글 또는 댓글을 작성할 수 없습니다.");
        }
    }


}
