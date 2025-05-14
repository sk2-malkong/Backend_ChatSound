package com.example.final_backend.service;

import com.example.final_backend.entity.BadwordLogEntity;
import com.example.final_backend.entity.PenaltyCountEntity;
import com.example.final_backend.entity.UserEntity;
import com.example.final_backend.entity.UserLimitsEntity;
import com.example.final_backend.repository.UserRepository;
import com.example.final_backend.repository.PenaltyCountRepository;
import com.example.final_backend.repository.UserLimitsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 사용자 비속어 사용 횟수 증가 및 사용자 이용 제한 관리 서비스
 */

@Service
@RequiredArgsConstructor
public class UserPenaltyService {
    private final PenaltyCountRepository penaltyCountRepository;
    private final UserLimitsRepository userLimitsRepository;
    private final UserRepository userRepository;

    // 사용자 욕설 감지 횟수 조회
    public int getPenaltyCount(String userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return user.getPenaltyCount() != null ? user.getPenaltyCount().getPenaltyCount() : 0;
    }

    // 사용자 제한 정보 조회 및 로그 조회 메서드
    public Map<String, Object> getLimitInfo(String userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Map<String, Object> result = new HashMap<>();
        UserLimitsEntity limit = user.getLimits();

        if (limit != null) {
            result.put("startDate", limit.getStartDate());
            result.put("endDate", limit.getEndDate());
            result.put("isActive", limit.getIsActive());
        } else {
            result.put("message", "제한 기록 없음");
        }

        List<BadwordLogEntity> logs = user.getBadwordLogs();
        result.put("logGroups", groupLogsByFive(logs, user));

        return result;
    }

    // 5개씩 로그 그룹화
    private List<Map<String, Object>> groupLogsByFive(List<BadwordLogEntity> logs, UserEntity user) {
        List<Map<String, Object>> groups = new ArrayList<>();
        if (logs == null || logs.size() < 5) return List.of();

        // 사용자 제한 시작 및 종료 시간 가져오기
        LocalDateTime start = (user.getLimits() != null) ? user.getLimits().getStartDate() : null;
        LocalDateTime end = (user.getLimits() != null) ? user.getLimits().getEndDate() : null;

        for (int i = 0; i + 4 < logs.size(); i += 5) {
            List<Map<String, Object>> group = new ArrayList<>();
            for (int j = i; j < i + 5; j++) {
                BadwordLogEntity log = logs.get(j);
                group.add(Map.of(
                        "originalWord", log.getOriginalWord(),
                        "filteredWord", log.getFilteredWord(),
                        "createdAt", log.getCreatedAt()
                ));
            }

            // endDate를 제한 종료 시간으로 설정
            groups.add(Map.of(
                    "startDate", start,
                    "endDate", end,
                    "logs", group
            ));
        }

        return groups;
    }

    // 사용자 패널티 관리
    @Transactional
    public void applyPenalty(int userId) {
        UserEntity user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        // 기존 패널티 불러오기
        PenaltyCountEntity existingPenalty = penaltyCountRepository.findByUserId(userId).orElse(null);

        // 패널티 증가 및 저장
        PenaltyCountEntity updatedPenalty = PenaltyCountEntity.incrementPenalty(user, existingPenalty);
        penaltyCountRepository.save(updatedPenalty);

        // 제한 처리
        int penaltyCount = updatedPenalty.getPenaltyCount();
        UserLimitsEntity limit = userLimitsRepository.findByUserId(userId).orElse(null);

        UserLimitsEntity.applyLimitIfNeeded(user, limit, penaltyCount)
                .ifPresent(userLimitsRepository::save);
    }

    // 사용자 제한 상태 확인
    public void checkUserLimit(UserEntity user) {
        if (user.getLimits() != null) {
            user.getLimits().checkAndReleaseIfExpired();
            userLimitsRepository.save(user.getLimits());  // 변경된 상태 DB에 저장

            System.out.println("24시간 제한 자동 해제됨");
        }
    }
}