package com.springjpatest.blog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 블로그 서비스 Spring Security 설정.
 *
 * <p>게이트웨이가 JWT를 이미 검증하고 X-User-Email 헤더를 전달하므로,
 * blog-service는 게이트웨이로부터의 요청만 허용하는 방향으로 설정한다.
 * CSRF는 REST API 특성상 비활성화하고, 세션도 사용하지 않는다.
 * </p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF: REST API는 세션이 없으므로 비활성화
            .csrf(AbstractHttpConfigurer::disable)

            // 세션 미사용: JWT 기반 무상태(stateless) 인증
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 요청 권한 설정
            .authorizeHttpRequests(auth -> auth
                // 공개 게시글 조회는 인증 없이 허용
                .requestMatchers(HttpMethod.GET, "/api/posts/**").permitAll()
                // H2 콘솔 접근 허용 (개발 환경)
                .requestMatchers("/h2-console/**").permitAll()
                // 나머지 모든 요청은 인증 필요
                // (게이트웨이가 X-User-Email 헤더를 전달하지 않으면 컨트롤러에서 400 에러)
                .anyRequest().permitAll()
            )

            // H2 콘솔을 iframe으로 표시하기 위해 X-Frame-Options 비활성화 (개발 환경)
            .headers(headers ->
                headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }
}
