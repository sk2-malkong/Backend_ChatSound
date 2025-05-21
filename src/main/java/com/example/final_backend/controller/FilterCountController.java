package com.example.final_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/filter")
public class FilterCountController {

    private final RedisTemplate<String, String> redisTemplate;

    @GetMapping("/count")
    public ResponseEntity<Integer> getFilterCount() {
        String count = redisTemplate.opsForValue().get("filter:count");
        return ResponseEntity.ok(count != null ? Integer.parseInt(count) : 0);
    }
}