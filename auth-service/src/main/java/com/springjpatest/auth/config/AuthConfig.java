package com.springjpatest.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * 인증 서비스 JPA 설정 클래스.
 *
 * <p>JPA Auditing을 활성화하여 User 엔티티의 @CreatedDate가
 * 자동으로 기록되도록 설정한다.
 * </p>
 */
@Configuration
@EnableJpaAuditing // createdAt 자동 기록 활성화
public class AuthConfig {
}
