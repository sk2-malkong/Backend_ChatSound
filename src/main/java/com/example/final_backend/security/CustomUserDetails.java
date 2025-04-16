package com.example.final_backend.security;

import com.example.final_backend.entity.UserEntity;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * 사용자 정보를 Spring Security에서 사용할 수 있게 변환해주는 객체
 * 
 * UserDetails 구현 클래스 (인증 정보를 담는 객체)
 * - UserEntity를 래핑하여 Spring Security와 연동
 *  - Security에서 사용할 수 있는 사용자 정보 제공
 * 
 * - UserDetailsServiceImpl -> loadUserByUsername()에서 사용자 정보 불러옴
 * - CusomUserDetails로 래핑하여 Spring Security에게 넘김
 * - 인증 성공 시 SecurityContextHolder에 객체 저장 후 인증된 사용자로 간주
 */

@Getter
public class CustomUserDetails implements UserDetails {

    private final UserEntity user;

    public CustomUserDetails(UserEntity user) {
        this.user = user;
    }

    public String getId() {
        return user.getId();  // 로그인 식별자
    }

    @Override
    public String getUsername() {
        return user.getId();  // ID가 username 역할
    }

    @Override
    public String getPassword() {
        return user.getPw();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
