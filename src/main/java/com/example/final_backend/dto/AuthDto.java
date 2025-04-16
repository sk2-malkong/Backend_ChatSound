package com.example.final_backend.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 회원가입, 정보 수정 시 사용하는 DTO
 */

@Getter
@Setter
public class AuthDto {
    private String id;
    private String username;
    private String email;
    private String pw;
    private String profileImage;
}