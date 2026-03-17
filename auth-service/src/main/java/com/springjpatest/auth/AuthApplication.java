package com.springjpatest.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 인증 서비스 진입점.
 *
 * <p>회원가입, 로그인, JWT 토큰 발급/갱신을 담당하는 독립 마이크로서비스.
 * 게이트웨이(8080)를 통해 /api/auth/** 요청을 처리한다.
 * </p>
 */
@SpringBootApplication
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
