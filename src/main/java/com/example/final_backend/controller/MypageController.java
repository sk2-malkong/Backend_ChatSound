package com.example.final_backend.controller;

import com.example.final_backend.dto.ProfileDto;
import com.example.final_backend.security.CustomUserDetails;
import com.example.final_backend.service.UserPenaltyService;
import com.example.final_backend.service.MypageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * /api/user로 시작하는 마이 페이지 관련 REST API 제공하는 컨트롤러
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class MypageController {
    private final MypageService mypageService;
    private final UserPenaltyService userPenaltyService;

    // 프로필 조회
    @Operation(summary = "프로필 조회", description = "로그인한 사용자의 프로필 정보를 반환합니다.")
    @ApiResponse(responseCode = "200", description = "프로필 조회 성공")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/profile")
    public ResponseEntity<ProfileDto.UserProfile> getProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        ProfileDto.UserProfile profileDto = mypageService.getProfile(userDetails.getId());
        return ResponseEntity.ok(profileDto);
    }


    // 프로필 수정
    @Operation(summary = "프로필 수정", description = "닉네임 또는 프로필 이미지를 수정합니다.")
    @ApiResponse(responseCode = "200", description = "프로필 수정 성공")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/profile")
    public ResponseEntity<String> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ProfileDto.UpdateProfile profileDto
    ) {
        mypageService.updateProfile(userDetails.getId(), profileDto);
        return ResponseEntity.ok("프로필이 성공적으로 수정되었습니다.");
    }

    // 프로필이미지 업로드
    @Operation(
            summary = "프로필 이미지 업로드",
            description = "사용자의 프로필 이미지를 업로드합니다."
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/profile/upload")
    public ResponseEntity<String> uploadProfileImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(
                    description = "업로드할 이미지 파일",
                    required = true,
                    schema = @Schema(type = "string", format = "binary")
            )
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        String imageUrl = mypageService.uploadProfileImage(userDetails.getId(), file);
        return ResponseEntity.ok("이미지 업로드 성공: " + imageUrl);
    }


    // 회원 탈퇴
    @Operation(summary = "회원 탈퇴", description = "로그인한 사용자의 계정을 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "회원 탈퇴가 완료되었습니다.")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteUser(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        mypageService.deleteUser(userDetails.getId());
        return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
    }


    // 패널티 횟수 조회
    @Operation(summary = "패널티 횟수 조회", description = "로그인한 사용자의 누적 패널티 횟수를 반환합니다.")
    @ApiResponse(responseCode = "200", description = "패널티 횟수가 성공적으로 반환됩니다.")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/penaltyCount")
    public ResponseEntity<Integer> getPenaltyCount(@AuthenticationPrincipal CustomUserDetails userDetails) {
        int count = userPenaltyService.getPenaltyCount(userDetails.getId());
        return ResponseEntity.ok(count);
    }


    // 제한 정보 조회
    @PostMapping("/limits")
    @Operation(summary = "제한 정보 조회", description = "로그인한 사용자의 서비스 이용 제한 정보를 반환합니다.")
    @ApiResponse(responseCode = "200", description = "제한 정보가 성공적으로 반환됩니다.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, Object>> getLimitInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Map<String, Object> result = userPenaltyService.getLimitInfo(userDetails.getId());
        return ResponseEntity.ok(result);
    }
}
