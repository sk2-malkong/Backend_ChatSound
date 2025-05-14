package com.example.final_backend.service;

import com.example.final_backend.entity.UserLimitsEntity;
import com.example.final_backend.factory.UserFactory;
import com.example.final_backend.repository.UserRepository;
import com.example.final_backend.dto.AuthDto;
import com.example.final_backend.dto.JwtDto;
import com.example.final_backend.entity.UserEntity;
import com.example.final_backend.repository.UserLimitsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 사용자의 인증 로직 처리 서비스
 * - 회원가입
 * - 로그인
 * - 로그아웃
 * - 토큰 재발급
 * - 닉네임 중복 확인
 * - 아이디 중복 확인
 * - 아이디 찾기
 * - 비밀번호 재설정
 */

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserLimitsRepository userLimitsRepository;
    private final AsyncService asyncService;

    // 회원가입
    @Transactional
    public void signup(AuthDto authDto) {
        // 아이디 중복 확인
        if (isIdDuplicate(authDto.getId())) {
            throw new IllegalArgumentException("아이디가 중복입니다.");
        }

        // 이메일 중복 확인
        if (userRepository.findByEmail(authDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이메일이 중복입니다.");
        }

        // 비밀번호 암호화
        String encodedPw = passwordEncoder.encode(authDto.getPw());

        // 정적 팩토리 메서드로 UserEntity + 연관 엔티티 생성(PenaltyCountEntity, UserLimitsEntity)
        UserEntity user = UserFactory.createWithDefaults(authDto, encodedPw);

        userRepository.save(user); // cascade 덕분에 연관 객체도 함께 저장됨

        // 비동기로 이메일 전송
        asyncService.sendWelcomeEmailAsync(user.getEmail(), user.getUsername());
    }



    // 로그인
    public AuthDto.LoginResponse login(AuthDto.LoginRequest loginRequest) {
        try {
            UserEntity user = userRepository.findById(loginRequest.getId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            // UserLimits에서 endDate, isActive 가져오기
            UserLimitsEntity limit = userLimitsRepository.findByUserId(user.getUserId()).orElse(null);
            LocalDateTime endDate = (limit != null) ? limit.getEndDate() : null;
            Boolean isActive = (limit != null) ? limit.getIsActive() : true;

            // 토큰 발급
            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            return AuthDto.LoginResponse.of(user, accessToken, refreshToken, endDate, isActive);

        } catch (AuthenticationException e) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }
    }

    // 토큰 재발급
    public JwtDto.TokenResponse updateToken(String refreshToken) {
        // 1. RefreshToken 유효성 검증 - 조작되거나 만료된 토큰인지 확인하는 단계
        String userId = jwtService.extractUsername(refreshToken);
        if (userId == null) {
            throw new IllegalArgumentException("RefreshToken이 유효하지 않거나 만료된 토큰입니다.");
        }

        // 2. Redis에 저장된 RefreshToken과 비교
        if (!jwtService.validateRefreshToken(refreshToken, userId)) {
            throw new IllegalArgumentException("RefreshToken이 유효하지 않습니다.");
        }

        // 3. 사용자 조회
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 4. 새로운 AccessToken 발급
        String newAccessToken = jwtService.reissueAccessToken(user);

        return JwtDto.TokenResponse.of(user, newAccessToken, refreshToken);
    }

    // 로그아웃
    public void logout(String accessToken) {
        // 헤더에서 토큰 추출 후 해당 토큰은 Redis 블랙리스트에 등록, RefreshToken 삭제
        if (accessToken != null && accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7);
        }

        String userId = jwtService.extractUsername(accessToken);
        if (userId != null) {
            jwtService.logout(userId, accessToken);
        }
    }

    // 아이디 조회
    public boolean isIdDuplicate(String id) {
        return userRepository.findById(id).isPresent();
    }

    // 닉네임 조회
    public boolean isNameDuplicate(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    // 아이디 찾기
    public String findIdByEmail(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일로 가입된 사용자가 없습니다."));
        return user.getId();  // 로그인용 ID 반환
    }

    // 비밀번호 재설정
    @Transactional
    public void resetPassword(String id, String email, String newPw) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("아이디가 존재하지 않습니다."));

        if (!user.getEmail().equals(email)) {
            throw new IllegalArgumentException("이메일이 일치하지 않습니다.");
        }

        user.setPw(passwordEncoder.encode(newPw));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

}