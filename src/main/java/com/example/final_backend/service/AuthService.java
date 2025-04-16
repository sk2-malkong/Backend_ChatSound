package com.example.final_backend.service;

import com.example.final_backend.repository.AuthRepository;
import com.example.final_backend.dto.AuthDto;
import com.example.final_backend.dto.JwtDto;
import com.example.final_backend.entity.UserEntity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
    private final AuthRepository authRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RedisService redisService;

    // 회원가입
    @Transactional
    public void signup(AuthDto dto) {
        // 이메일 중복 확인
        if (authRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
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

        // 이메일 전송
        sendWelcomeEmail(user.getEmail(), user.getUsername());
    }

    // 로그인
    public JwtDto.TokenResponse login(JwtDto.LoginRequest loginRequest) {
        try {
            // authenticationManager를 이용해 ID/PW 인증 시도
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getId(),
                            loginRequest.getPw()
                    )
            );

            UserEntity user = authRepository.findById(loginRequest.getId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));


            // 인증 성공 시 AccessToken과 RefreshToken을 JWT로 생성
            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            return JwtDto.TokenResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .id(user.getId())
                    .username(user.getUsername())
                    .tokenType("Bearer")
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

    // 이메일 인증 전송
    private void sendWelcomeEmail(String email, String username) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("회원가입을 환영합니다!");
        message.setText(username + "님, 환영합니다. 가입을 축하드립니다!");
        mailSender.send(message);
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