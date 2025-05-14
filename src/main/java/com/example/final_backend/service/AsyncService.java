package com.example.final_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 이메일 전송 작업 비동기 처리
 * - 회원가입 시 환영 이메일 비동기로 발송
 * - @Async 어노테이션을 통해 별도 스레드에서 처리하도록 함
 */

@Service
@RequiredArgsConstructor
public class AsyncService {
    private final JavaMailSender mailSender;

    @Async
    public void sendWelcomeEmailAsync(String email, String username) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("회원가입을 환영합니다!");
        message.setText(username + "님, 환영합니다. 가입을 축하드립니다!");
        mailSender.send(message);
    }
}
