package com.example.final_backend.security;

import com.example.final_backend.service.JwtService;
import com.example.final_backend.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 매 요청마다 JWT 검사 -> 검증 성공 시 인증 정보 저장 후 인가 처리를 가능하게 함
 * 
 * 모든 요청 전에 실행되는 JWT 인증 필터(OncePerRequestFilter 상속)
 * - 사용자가 요청할 때 JWT 토큰이 유효한지 확인하고, 인증된 사용자로 등록하는 JWT 인증 필터
 * - Authorization 헤더에서 토큰 추출 -> 검증 -> SecuriyContext 등록
 */

@Component
@RequiredArgsConstructor
                                         // OncePerRequestFilter (요청 1건당 1회만 실행)
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 헤더에서 토큰 추출
        // ex: "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI..."
        String authHeader = request.getHeader("Authorization");
        // 유효한 Bearer 토큰인지 확인
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // 그냥 넘김
            return;
        }

        // 2. 토큰에서 userId 추출
        String token = authHeader.substring(7); // "Bearer " 이후 실제 토큰만 추출
        String userId = jwtService.extractUsername(token);

        // 3. SecurityContext에 인증이 없는 경우에만 실행
        if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 4. DB에서 사용자 조회 -> 토큰 검증
            UserDetails userDetails = userDetailsService.loadUserByUsername(userId);

            // validateAccessToken 메서드는 블랙리스트 확인을 포함
            if (jwtService.validateAccessToken(token, userDetails)) {
                // 5. 인증 객체 생성 및 등록
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 6. 다음 필터로 진행
        filterChain.doFilter(request, response);
    }
}
