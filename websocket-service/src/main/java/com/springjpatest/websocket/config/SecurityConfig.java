package com.springjpatest.websocket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 웹소켓 서비스 Spring Security 설정.
 *
 * <p>WebSocket 핸드셰이크는 HTTP 요청으로 시작되므로 Spring Security가 개입한다.
 * 게이트웨이에서 JWT를 이미 검증하고 X-User-* 헤더를 전달하므로,
 * 이 서비스에서는 모든 요청을 허용하되 CSRF만 비활성화한다.
 * </p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF: WebSocket + REST API는 토큰 기반이므로 비활성화
            .csrf(AbstractHttpConfigurer::disable)

            // 세션 미사용: WebSocket 연결 상태는 별도 관리
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // WebSocket 핸드셰이크(/ws/**) 및 채팅 API 전부 허용
            // (게이트웨이가 JWT를 1차 검증하고 라우팅)
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );

        return http.build();
    }
}
