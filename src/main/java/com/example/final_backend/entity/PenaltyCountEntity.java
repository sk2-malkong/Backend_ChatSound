package com.example.final_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 패널티 횟수 Entity
 */

@Entity
@Data
@Table(name="penaltyCounts")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PenaltyCountEntity {
    // 사용자 식별 번호
    @Id
    private int userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "userId")
    private UserEntity user;

    // 패널티 횟수 기록
    private int penaltyCount;

    // 패널티 횟수 업데이트 일자
    private LocalDate lastUpdate;

    // 패널티 증가 및 저장을 위한 정적 메서드
    public static PenaltyCountEntity incrementPenalty(UserEntity user, PenaltyCountEntity existing) {
        PenaltyCountEntity penalty = (existing != null) ? existing : new PenaltyCountEntity();

        penalty.setUser(user);
        penalty.setUserId(user.getUserId());

        int current = penalty.getPenaltyCount();
        penalty.setPenaltyCount(current + 1);
        penalty.setLastUpdate(LocalDate.now());

        return penalty;
    }
}
