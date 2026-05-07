package com.springjpatest.itemservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * 아이템 서비스 메인 애플리케이션.
 *
 * <p>H2 인메모리 DB에서 아이템 데이터를 관리한다.
 * 목록 조회 캐싱(@EnableCaching)과 JPA Auditing(@EnableJpaAuditing)을 활성화한다.
 * </p>
 */
@SpringBootApplication
@EnableCaching    // Spring Cache 활성화 (목록 조회 캐싱)
@EnableJpaAuditing // @CreatedDate, @LastModifiedDate 자동 처리
public class ItemServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(ItemServiceApplication.class, args);
  }
}
