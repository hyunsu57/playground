package com.springjpatest.itemservice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * 애플리케이션 컨텍스트 로드 테스트.
 *
 * <p>전체 Spring 컨텍스트가 오류 없이 로드되는지 확인하는 스모크 테스트.
 * 빈 설정, 자동구성, 컴포넌트 스캔 등에 문제가 없으면 통과한다.
 * </p>
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.sql.init.mode=never",
    "spring.jpa.defer-datasource-initialization=true"
})
class ItemServiceApplicationTests {

  @Test
  @DisplayName("Spring 컨텍스트가 정상적으로 로드된다")
  void contextLoads() {
    // 컨텍스트 로드만 확인하는 스모크 테스트 — 내용 없음
  }
}
