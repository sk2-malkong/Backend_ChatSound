package com.example.final_backend.config;

import com.example.final_backend.security.JwtAuthorizationFilter;
import com.example.final_backend.service.UserDetailsServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Spring Security 설정
 * - JWT 필터 등록(JwtAuthorizationFilter)
 * - 인증 예외 처리
 * - 공개 URL 설정 /auth/** 인증 없이 접근 가능
 * - CORS 설정(localhost:3000)
 */

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthorizationFilter jwtAuthorizationFilter;

    // 비밀번호 암호화
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 인증 매니저 등록
    // authenticationManager.authenticate가
    // DaoAuthenticationProvider.authenticate() 호출하게 되면
    // 내부에서 UserDetailsServiceImpl.java 에 있는
    // UserDetailsServiceImpl.loadUserByUsername(id)를
    // 자동 호출해 DB에서 사용자 정보를
    // ID + PW를 기반으로 인증 프로세스를 시작하게 되는데
    // 인증이 완료 되면 UserEntity를 기반으로 CustomUserDetails가 생성됨
                                                      // AuthenticationConfiguration authConfig는
                                                      // Spring이 자동 생성한 AuthenticationManager를
    @Bean                                             // getAuthenticationManager()로 꺼내 쓰기 위해서 사용
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        // 로그인 인증 처리에 사용(AuthService에서 사용)
        // 내부에 DaoAuthenticationProvider가 들어 있어서 반환시 실행됨
        return authConfig.getAuthenticationManager();
    }

    // 보안 필터 체인 설정
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1) CORS 설정 추가
                .cors(cors -> {})
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 사용자 관리 관련 인증 경로
                        .requestMatchers("/api/auth/**",
                                // Swagger 권한 허용
                                "/v3/api-docs/**",             // OpenAPI 문서
                                "/swagger-ui/**",              // Swagger UI
                                "/swagger-ui.html",            // 예전 버전
                                "/swagger-resources/**",       // Swagger 리소스
                                "/webjars/**"                  // Swagger 정적 리소스
                        ).permitAll()

                        // 검색 관련 인증 경로
                        .requestMatchers(HttpMethod.GET, "/api/search").permitAll()

                        // 게시글 관련 인증 경로
                        .requestMatchers(HttpMethod.GET, "/api/post/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/post/create").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/post/update/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/post/delete/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/post/my").authenticated()


                        // 유저 관련 인증 경로
                        .requestMatchers("/user/**").authenticated()

                        // 댓글 관련 인증 경로
                        .requestMatchers(HttpMethod.GET, "/api/comment/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/comment/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/comment/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/comment/**").authenticated()

                        // 비속어 횟수 조회 인증 경로
                        .requestMatchers(HttpMethod.GET, "/api/user/penalty").permitAll()

                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            System.out.println("접근 거부됨! 이유: " + accessDeniedException.getMessage());
                            response.sendError(HttpServletResponse.SC_FORBIDDEN);
                        })
                )
                // 2) JWT 필터 추가(기존 코드와 동일)
                // JwtAuthorizationFilter는 UsernamePasswordAuthenticationFilter 보다 먼저 실행
                .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }

    // CORS 세부 설정(예: 로컬 프론트 3000 허용)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("http://localhost:3000"); // 프론트 주소
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true); //쿠키, 인증 헤더 등을 프론트엔드에서 사용할 수 있도록 허용

        configuration.addExposedHeader("Authorization");
        configuration.addExposedHeader("Refresh-Token");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}