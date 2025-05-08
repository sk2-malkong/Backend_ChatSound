package com.example.final_backend.service;

import com.example.final_backend.dto.PenaltyResponseDto;
import com.example.final_backend.entity.PenaltyCountEntity;
import com.example.final_backend.repository.PenaltyCountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PenaltyCountService {

    private final PenaltyCountRepository penaltyCountRepository;

    // 개별 유저 패널티 조회
    public PenaltyResponseDto getPenaltyByUserId(int userId) {
        PenaltyCountEntity entity = penaltyCountRepository.findByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException("해당 유저의 패널티 기록이 존재하지 않습니다."));

        return PenaltyResponseDto.builder()
                .penaltyCount(entity.getPenaltyCount())
                .build();
    }

    // 전체 유저 패널티 조회
    public Map<String, Object> getAllPenaltyCounts() {
        List<PenaltyCountEntity> counts = penaltyCountRepository.findAll();

        int total = counts.stream()
                .mapToInt(PenaltyCountEntity::getPenaltyCount)
                .sum();

        Map<String, Object> result = new HashMap<>();
        result.put("penaltyAllCount", total);
        return result;
    }

}
