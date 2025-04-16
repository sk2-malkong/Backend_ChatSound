package com.example.final_backend.controller;

import com.example.final_backend.dto.AuthDto;
import com.example.final_backend.entity.UserEntity;
import com.example.final_backend.security.CustomUserDetails;
import com.example.final_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;

    // ✅ 프로필 조회
    @GetMapping("/profile")
    public ResponseEntity<UserEntity> getProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(userService.getProfile(userDetails.getId()));
    }

    // ✅ 프로필 수정
    @PutMapping("/profile")
    public ResponseEntity<String> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody AuthDto dto
    ) {
        userService.updateProfile(userDetails.getId(), dto);
        return ResponseEntity.ok("프로필이 성공적으로 수정되었습니다.");
    }

    @PostMapping("/profile/upload")
    public ResponseEntity<String> uploadProfileImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("file") MultipartFile file
    ) {
        String imageUrl = userService.uploadProfileImage(userDetails.getId(), file);
        return ResponseEntity.ok("이미지 업로드 성공: " + imageUrl);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteUser(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        userService.deleteUser(userDetails.getId());
        return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
    }

    @PostMapping("/penaltyCount")
    public ResponseEntity<Integer> getPenaltyCount(@AuthenticationPrincipal CustomUserDetails userDetails) {
        int count = userService.getPenaltyCount(userDetails.getId());
        return ResponseEntity.ok(count);
    }

    @PostMapping("/limits")
    public ResponseEntity<Map<String, Object>> getLimitInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Map<String, Object> result = userService.getLimitInfo(userDetails.getId());
        return ResponseEntity.ok(result);
    }

}
