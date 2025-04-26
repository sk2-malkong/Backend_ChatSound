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
// implements UserDetails: Spring Security에서 인증에 사용할 수 있도록 UserDetails 인터페이스 구현
public class CustomUserDetails implements UserDetails {

    private final UserEntity user;
    // 외부에서 UserEntity를 주입받아 CustomUserDetails로 감쌈
    public CustomUserDetails(UserEntity user) {
        this.user = user;
    }

    public String getId() {

        return user.getId();  // 로그인 식별자
    }

    //UserDetails 필수 구현 메서드
    @Override
    public String getUsername() {

        return user.getId();  // ID가 username 역할
    }

    @Override
    public String getPassword() {
        return user.getPw();
    }

    // 왜 각 유저마다 ROLE_USER를 부여하는가?
    // 현재 프로젝트는 일반 사용자만 존재하고 관리자가 없기 때문에
    // 사용자 권한이 단일해서 ROLE_USER만 부여.

    // ROLE_USER의 역할
    // 	Spring Security에서는 이 ROLE_ 접두사가 붙은 문자열을
    // 	기준으로 접근 권한을 판단함.
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }
    //메서드	true로 고정한 이유
    //isAccountNonExpired()	계정 만료 기능이 필요 없는 경우 기본값 true 처리
    //isAccountNonLocked()	사용자 정지 기능이 없는 경우 잠금 상태 관리 안 함
    //isCredentialsNonExpired()	비밀번호 유효기간 정책 미적용 시 사용 안 함
    //isEnabled()	계정 비활성화(탈퇴 등)를 DB로 관리하지 않을 경우 항상 활성화
    @Override public boolean isAccountNonExpired() { return true; } // 계정 만료 기능
    @Override public boolean isAccountNonLocked() { return true; } // 사용자 정지 기능
    @Override public boolean isCredentialsNonExpired() { return true; } // 비밀번호 유효기간
    @Override public boolean isEnabled() { return true; } // 계정 비활성화
}
