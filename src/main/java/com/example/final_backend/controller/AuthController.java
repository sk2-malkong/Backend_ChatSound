package com.example.final_backend.controller;

import com.example.final_backend.dto.AuthDto;
import com.example.final_backend.dto.JwtDto;
import com.example.final_backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * /api/auth로 시작하는 인증 관련 REST API 제공하는 컨트롤러
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    // 회원가입
    @Operation(summary = "회원가입", description = "회원 정보를 받아 회원가입을 진행합니다.")
    @ApiResponse(responseCode = "200", description = "회원가입 성공")
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody AuthDto dto) {
        try {
            authService.signup(dto);
            return ResponseEntity.ok("회원가입이 완료되었습니다");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 로그인
    @Operation(summary = "로그인", description = "회원 정보를 받아 로그인을 진행하고 로그인 성공 시 토큰을 발급합니다.")
    @ApiResponse(responseCode = "200", description = "로그인 성공")
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody AuthDto.LoginRequest loginRequest) {
        try {
            AuthDto.LoginResponse response = authService.login(loginRequest);

            // 응답 바디에는 필요한 최소 정보만 전달 (isActive)
            Map<String, Object> body = new HashMap<>();
            body.put("message", "로그인 성공");
            body.put("endDate", response.getEndDate());
            body.put("isActive", response.getIsActive());

            return ResponseEntity.ok()
                    .header("Authorization", "Bearer " + response.getAccessToken())
                    .header("Refresh-Token", response.getRefreshToken())
                    .body(body);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "로그인 실패");
            return ResponseEntity.badRequest().body(error);
        }
    }


    // Access 토큰 재발급
    @Operation(summary = "토큰 재발급", description = "Refresh Token을 받아 인가 확인 후 토큰을 재발급합니다.")
    @ApiResponse(responseCode = "200", description = "토큰 재발급 성공")
    @PostMapping("/refresh")
    public ResponseEntity<String> updateToken(@RequestBody JwtDto.RefreshTokenRequest request) {
        try {
            JwtDto.TokenResponse response = authService.updateToken(request.getRefreshToken());

            return ResponseEntity.ok()
                    .header("Authorization", "Bearer " + response.getAccessToken())
                    .header("Refresh-Token", response.getRefreshToken())
                    .body("토큰 재발급 성공");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("토큰 재발급 실패");
        }
    }


    // 로그아웃
    @Operation(summary = "로그아웃", description = "회원 정보를 받아 로그아웃을 진행하고 발급된 토큰을 삭제 및 블랙리스트 처리합니다.")
    @ApiResponse(responseCode = "200", description = "로그아웃 성공")
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String accessToken) {
        authService.logout(accessToken);
        return ResponseEntity.ok("로그아웃 되었습니다.");
    }

    // 아이디 중복 검사
    @Operation(summary = "아이디 중복 검사", description = "사용자가 입력한 ID가 중복되었는지 확인합니다.")
    @ApiResponse(responseCode = "200", description = "아이디 중복 검사 성공")
    @GetMapping("/checkId")
    public ResponseEntity<String> checkId(@RequestParam String id) {
        boolean isDuplicate = authService.isIdDuplicate(id);
        if (isDuplicate) {
            return ResponseEntity.badRequest().body("이미 사용 중인 ID 입니다.");
        }
        return ResponseEntity.ok("사용 가능한 ID 입니다.");
    }

    // 닉네임 중복 검사
    @Operation(summary = "닉네임 중복 검사", description = "사용자가 입력한 닉네임이 중복되었는지 확인합니다.")
    @ApiResponse(responseCode = "200", description = "닉네임 중복 검사 성공")
    @GetMapping("/checkName")
    public ResponseEntity<String> checkName(@RequestParam String username) {
        boolean isDuplicate = authService.isNameDuplicate(username);
        if (isDuplicate) {
            return ResponseEntity.badRequest().body("이미 사용 중인 닉네임 입니다.");
        }
        return ResponseEntity.ok("사용 가능한 닉네임 입니다.");
    }

    // 아이디 찾기
    @Operation(summary = "아이디 찾기", description = "사용자가 입력한 ID가 기존에 존재했는지 확인 합니다.")
    @ApiResponse(responseCode = "200", description = "아이디 찾기 성공")
    @PostMapping("/findId")
    public ResponseEntity<String> findIdByEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String id = authService.findIdByEmail(email);
        return ResponseEntity.ok(id);
    }

    // 비밀번호 재설정
    @Operation(summary = "비밀번호 재설정", description = "사용자의 id와 email을 확인해 사용자가 원하는 비밀번호로 재설정 합니다.")
    @ApiResponse(responseCode = "200", description = "비밀번호 재설정 성공")
    @PostMapping("/resetPassword")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> request) {
        String id = request.get("id");
        String email = request.get("email");
        String newPw = request.get("newPw");
        authService.resetPassword(id, email, newPw);
        return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
    }
}
