package com.example.final_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;

/**
 * 사용자 권한 제한 Entity
 */

@Entity
@Table(name="limits")
@NoArgsConstructor
@AllArgsConstructor
@Data
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
}
