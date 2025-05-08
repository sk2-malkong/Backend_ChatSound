package com.example.final_backend.controller;

import com.example.final_backend.dto.PenaltyResponseDto;
import com.example.final_backend.entity.PenaltyCountEntity;
import com.example.final_backend.service.PenaltyCountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/penalty")
public class PenaltyCountController {

    private final PenaltyCountService penaltyCountService;

    @GetMapping("/{userId}")
    public ResponseEntity<PenaltyResponseDto> getPenalty(@PathVariable int userId) {
        return ResponseEntity.ok(penaltyCountService.getPenaltyByUserId(userId));
    }

    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllPenalties() {
        return ResponseEntity.ok(penaltyCountService.getAllPenaltyCounts());
    }
}
