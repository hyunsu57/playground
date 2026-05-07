package com.springjpatest.multithreadservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Java 21 멀티스레드 기능 데모 서비스.
 *
 * 다루는 기능:
 * - Virtual Threads (JEP 444, Java 21 정식): 경량 가상 스레드
 * - Structured Concurrency (JEP 453, Java 21 프리뷰): 구조적 동시성
 * - Scoped Values (JEP 446, Java 21 프리뷰): 스코프 기반 값 전파
 *
 * application.yml의 spring.threads.virtual.enabled=true 설정으로
 * 내장 Tomcat과 @Async 작업이 모두 가상 스레드 위에서 실행된다.
 */
@SpringBootApplication
@EnableAsync
public class MultiThreadServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MultiThreadServiceApplication.class, args);
    }
}
