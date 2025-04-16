package com.example.final_backend.service;

import com.example.final_backend.repository.AuthRepository;
import com.example.final_backend.entity.UserEntity;
import com.example.final_backend.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Spring Security에서 사용자 인증을 위한 UserDetailsService의 구현체
 * - loadUserByUsername : DB에서 사용자 조회 후 CustomUserDetails 반환환
 */

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final AuthRepository authRepository;

    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        UserEntity user = authRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + id));

        return new CustomUserDetails(user);
    }
}
