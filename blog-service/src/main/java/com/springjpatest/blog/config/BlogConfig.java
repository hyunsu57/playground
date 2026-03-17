package com.springjpatest.blog.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * 블로그 서비스 설정 클래스.
 *
 * <p>JPA Auditing을 활성화하여 Post 엔티티의 @CreatedDate, @LastModifiedDate가
 * 자동으로 기록되도록 설정한다.
 * </p>
 */
@Configuration
@EnableJpaAuditing // createdAt, updatedAt 자동 기록 활성화
public class BlogConfig {
}
