package com.springjpatest.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * JWT 인증 전역 필터.
 *
 * <p>게이트웨이 레벨에서 모든 보호된 요청의 JWT 토큰을 1차 검증한다.
 * 유효한 토큰이면 사용자 정보(이메일)를 헤더에 추가하여 하위 서비스로 전달한다.
 * 인증이 불필요한 경로(/api/auth/**)는 검증 없이 통과시킨다.
 * </p>
 */
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    /** JWT 서명 검증에 사용할 비밀 키 (환경변수 JWT_SECRET 또는 기본값) */
    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * 인증 없이 통과할 경로 목록.
     * auth-service 엔드포인트는 로그인/회원가입이므로 JWT 불필요.
     */
    private static final List<String> WHITE_LIST = List.of(
        "/api/auth/login",
        "/api/auth/signup",
        "/api/auth/refresh"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 화이트리스트 경로는 JWT 검증 없이 통과
        boolean isWhiteListed = WHITE_LIST.stream().anyMatch(path::startsWith);
        if (isWhiteListed) {
            return chain.filter(exchange);
        }

        // Authorization 헤더에서 Bearer 토큰 추출
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // 토큰 없음 → 401 Unauthorized 반환
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7); // "Bearer " 이후 토큰 문자열

        try {
            // jjwt 0.12.x API: parseSignedClaims()로 서명 검증 및 클레임 파싱
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

            // 검증된 사용자 이메일을 헤더에 추가하여 하위 서비스로 전달
            // 하위 서비스는 이 헤더를 신뢰하여 추가 DB 조회 없이 사용자 식별 가능
            ServerWebExchange mutatedExchange = exchange.mutate()
                .request(req -> req.header("X-User-Email", claims.getSubject()))
                .build();

            return chain.filter(mutatedExchange);

        } catch (Exception e) {
            // 토큰 만료 또는 서명 불일치 → 401 반환
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        // 가장 먼저 실행되도록 높은 우선순위(낮은 숫자) 설정
        return -1;
    }
}
