package com.example.final_backend.entity;

import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 사용자 권한 제한 Entity
 */

@Entity
@Table(name="limits")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserLimitsEntity {
    // 제한된 사용자
    @Id
    private int userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "userId")
    private UserEntity user;

    // 제한 시작 일자
    private LocalDateTime startDate;

    // 제한 끝나는 일자
    private LocalDateTime endDate;

    // 기능 사용 가능 여부 (기본 true)
    private Boolean isActive;

    // 사용자 제한 처리를 위한 정적 메서드
    public static Optional<UserLimitsEntity> applyLimitIfNeeded(UserEntity user, UserLimitsEntity existing, int penaltyCount) {
        if (penaltyCount % 5 != 0) return Optional.empty();

        UserLimitsEntity limit = (existing != null) ? existing : new UserLimitsEntity();

        limit.setUser(user);
        limit.setUserId(user.getUserId());
        limit.setIsActive(false);
        limit.setStartDate(LocalDateTime.now());
        //limit.setEndDate(LocalDateTime.now().plusHours(24));  // 제한 시간 24 시간
        limit.setEndDate(LocalDateTime.now().plusMinutes(3)); // 제한 시간 3분

        return Optional.of(limit);
    }

    // 사용자 제한 상태 확인용 인스턴스 메서드
    public void checkAndReleaseIfExpired() {
        if (Boolean.FALSE.equals(this.isActive)) {
            LocalDateTime now = LocalDateTime.now();

            if (this.endDate != null && now.isAfter(this.endDate)) {
                this.isActive = true;
                this.startDate = null;
                this.endDate = null;
            } else {
                throw new IllegalStateException("욕설 사용 5회로 24시간 동안 게시글 또는 댓글을 작성할 수 없습니다.");
            }
        }
    }
}
