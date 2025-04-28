package com.example.final_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

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
