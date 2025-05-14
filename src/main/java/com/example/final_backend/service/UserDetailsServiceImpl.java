package com.example.final_backend.service;

import com.example.final_backend.repository.UserRepository;
import com.example.final_backend.entity.UserEntity;
import com.example.final_backend.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security에서 사용자 인증을 위한 UserDetailsService의 구현체
 * - loadUserByUsername : DB에서 사용자 조회 후 CustomUserDetails 반환
 */

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    //loadUserByUsername(String id)는
    //Spring Security가 로그인 처리 시 자동으로 호출하는 메서드
    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        //사용자 ID 기준으로 DB에서 조회
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + id));

        //UserEntity를 UserDetails 타입인 CustomUserDetails로 변환
        //이후 Spring Security 인증 흐름에서 사용됨
        return new CustomUserDetails(user);
    }
}
