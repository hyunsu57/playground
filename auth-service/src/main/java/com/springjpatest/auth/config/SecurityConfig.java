package com.springjpatest.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 인증 서비스 Spring Security 설정.
 *
 * <p>모든 /api/auth/** 엔드포인트는 공개 접근 허용.
 * BCryptPasswordEncoder를 Bean으로 등록하여 비밀번호 암호화에 사용한다.
 * </p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 비밀번호 암호화에 사용할 BCrypt 인코더.
     * strength 기본값(10)을 사용하여 보안과 성능의 균형을 맞춘다.
     *
     * @return BCryptPasswordEncoder 인스턴스
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF: REST API 특성상 비활성화
            .csrf(AbstractHttpConfigurer::disable)

            // 세션 미사용: JWT 기반 무상태(stateless) 설계
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 요청 권한 설정: 인증 서비스 자체는 모두 공개
            .authorizeHttpRequests(auth -> auth
                // /api/auth/** 모든 경로 인증 없이 허용 (로그인/회원가입 엔드포인트)
                .requestMatchers("/api/auth/**").permitAll()
                // H2 콘솔 허용 (개발 환경)
                .requestMatchers("/h2-console/**").permitAll()
                .anyRequest().authenticated()
            )

            // H2 콘솔 iframe 표시를 위해 X-Frame-Options 비활성화 (개발 환경)
            .headers(headers ->
                headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }
}
