package com.springjpatest.multithreadservice.config;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 가상 스레드 설정.
 *
 * spring.threads.virtual.enabled=true (application.yml) 설정으로
 * Spring Boot가 자동으로 Tomcat + @Async 를 가상 스레드로 구성하지만,
 * 아래 AsyncConfigurer 구현으로 @Async 작업의 실행자를 명시적으로 지정하여
 * "가상 스레드로 실행 중임"을 코드 수준에서 확인할 수 있다.
 *
 * 가상 스레드의 핵심 원리:
 * - OS 스레드(캐리어 스레드)와 분리된 JVM 관리 경량 스레드
 * - 블로킹 I/O 발생 시 OS 스레드를 점유하지 않고 즉시 반환 (언마운트)
 * - I/O 완료 후 다른 캐리어 스레드에 재스케줄 (마운트)
 * - 수백만 개의 동시 가상 스레드 생성 가능
 */
@Configuration
public class VirtualThreadConfig implements AsyncConfigurer {

    /**
     * @Async 애너테이션이 붙은 메서드는 이 실행자 위에서 동작한다.
     * newVirtualThreadPerTaskExecutor(): 작업마다 새 가상 스레드를 생성하는 실행자.
     * 가상 스레드는 생성 비용이 매우 낮으므로 풀링이 불필요하다.
     */
    @Override
    public Executor getAsyncExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * @Async 작업에서 처리되지 않은 예외 핸들러.
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) ->
            System.err.printf("[AsyncError] %s.%s 실패: %s%n",
                method.getDeclaringClass().getSimpleName(), method.getName(), ex.getMessage());
    }
}
