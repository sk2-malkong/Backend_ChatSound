package com.example.final_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name="limits")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserLimitsEntity {
    // 제한된 사용자
    @Id
    @OneToOne
    @JoinColumn(name = "users")
    private UserEntity userId;

    // 제한 시작 일자
    private LocalDateTime startDate;

    // 제한 끝나는 일자
    private LocalDateTime endDate;

    // 제한 여부 (기본 true)
    private Boolean isActive;
}
