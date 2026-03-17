package com.springjpatest.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API Gateway 서비스 진입점.
 *
 * <p>모든 클라이언트(프론트엔드) 요청을 받아서 적절한 백엔드 서비스로 라우팅한다.
 * <ul>
 *   <li>/api/posts/**, /api/categories/** → blog-service (8081)</li>
 *   <li>/api/auth/** → auth-service (8082)</li>
 * </ul>
 * </p>
 */
@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
