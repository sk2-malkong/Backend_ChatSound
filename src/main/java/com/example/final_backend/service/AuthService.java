package com.example.final_backend.service;

import com.example.final_backend.entity.PenaltyCountEntity;
import com.example.final_backend.entity.UserLimitsEntity;
import com.example.final_backend.repository.AuthRepository;
import com.example.final_backend.dto.AuthDto;
import com.example.final_backend.dto.JwtDto;
import com.example.final_backend.entity.UserEntity;
import com.example.final_backend.repository.PenaltyCountRepository;
import com.example.final_backend.repository.UserLimitsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserLimitsRepository userLimitsRepository;
    private final PenaltyCountRepository penaltyCountRepository;
    private final AsyncService asyncService;

    // 회원가입
    @Transactional
    public void signup(AuthDto dto) {
        // 아이디 중복 확인
        if (authRepository.findById(dto.getId()).isPresent()) {
            throw new IllegalArgumentException("아이디가 중복입니다.");
        }

        // 이메일 중복 확인
        if (authRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이메일이 중복입니다.");
        }

        // 사용자 엔티티 생성 및 저장
        UserEntity user = new UserEntity();
        user.setId(dto.getId());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPw(passwordEncoder.encode(dto.getPw())); // 비밀번호 암호화
        user.setProfileImage(dto.getProfileImage());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        authRepository.save(user);

        // 회원가입 시 기본 PenaltyCountEntity 생성
        PenaltyCountEntity penalty = new PenaltyCountEntity();
        penalty.setUser(user);
        penalty.setPenaltyCount(0);
        penalty.setLastUpdate(LocalDate.now());

        penaltyCountRepository.save(penalty);

        // 회원가입 시 기본 UserLimitsEntity 생성
        UserLimitsEntity limits = new UserLimitsEntity();
        limits.setUser(user);
        limits.setIsActive(true); // 기본값 활성화 상태
        limits.setStartDate(null);
        limits.setEndDate(null);

        userLimitsRepository.save(limits);

        // 비동기로 이메일 전송
        asyncService.sendWelcomeEmailAsync(user.getEmail(), user.getUsername());
    }



    // 로그인
    public JwtDto.TokenResponse login(JwtDto.LoginRequest loginRequest) {
        //사용자가 입력한 아이디와 비밀번호를 UsernamePasswordAuthenticationToken 객체로
        // 래핑하여 AuthenticationManager에게 전달함
        // 즉 AuthenticationManager에게  "이 사용자(id/pw)를 인증해줘"라고 요청하는 거임

        // authenticationManager를 이용해 ID/PW 인증 시도
        // 그 시도를 내부적으로 SecurityConfig.java에서
        // 내부의 DaoAuthenticationProvider.authenticate() 호출함

        //결국 authentication는 사용자가 입력한 id/pw로 인증 시도하고
        //그 결과로 인증 성공 여부,사용자 정보,권한이 담긴 Authentication를 반환 받는것이 목적
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getId(),
                            loginRequest.getPw()
                    )
            );

            UserEntity user = authRepository.findById(loginRequest.getId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            // UserLimits에서endDate 가져오기
            UserLimitsEntity limit = userLimitsRepository.findByUserId(user.getUserId()).orElse(null);
            LocalDateTime endDate = (limit != null) ? limit.getEndDate() : null;

            PenaltyCountEntity penaltyCountEntity = penaltyCountRepository.findByUserId(user.getUserId()).orElse(null);
            int penaltyCount = penaltyCountEntity.getPenaltyCount();

            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            return JwtDto.TokenResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .id(user.getId())
                    .username(user.getUsername())
                    .tokenType("Bearer")
                    .endDate(endDate)
                    .penaltyCount(penaltyCount)
                    .build();

        } catch (AuthenticationException e) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }
    }

    // 토큰 재발급
    public JwtDto.TokenResponse refreshToken(String refreshToken) {
        // 1. RefreshToken 유효성 검증
        String userId = jwtService.extractUsername(refreshToken);
        if (userId == null) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }

        // 2. Redis에 저장된 RefreshToken과 비교
        if (!jwtService.validateRefreshToken(refreshToken, userId)) {
            throw new IllegalArgumentException("RefreshToken이 유효하지 않습니다.");
        }

        // 3. 사용자 조회
        UserEntity user = authRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 4. 새로운 AccessToken 발급
        String newAccessToken = jwtService.reissueAccessToken(user);

        return JwtDto.TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken) // 기존 RefreshToken 유지
                .id(user.getId())
                .username(user.getUsername())
                .tokenType("Bearer")
                .build();
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
        return authRepository.findById(id).isPresent();
    }

    // 닉네임 조회
    public boolean isNameDuplicate(String username) {
        return authRepository.findByUsername(username).isPresent();
    }

    // 아이디 찾기
    public String findIdByEmail(String email) {
        UserEntity user = authRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일로 가입된 사용자가 없습니다."));
        return user.getId();  // 로그인용 ID 반환
    }

    // 비밀번호 재설정
    @Transactional
    public void resetPassword(String id, String email, String newPw) {
        UserEntity user = authRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("아이디가 존재하지 않습니다."));

        if (!user.getEmail().equals(email)) {
            throw new IllegalArgumentException("이메일이 일치하지 않습니다.");
        }

        user.setPw(passwordEncoder.encode(newPw));
        user.setUpdatedAt(LocalDateTime.now());
        authRepository.save(user);
    }

}