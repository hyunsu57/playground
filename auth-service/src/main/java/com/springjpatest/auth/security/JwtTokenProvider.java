package com.springjpatest.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 토큰 생성 및 검증 컴포넌트.
 *
 * <p>jjwt 0.12.x API를 사용하여 액세스 토큰과 리프레시 토큰을 생성한다.
 * 0.12.x에서는 기존 0.9.x 대비 API가 크게 변경됨:
 * <ul>
 *   <li>parseClaimsJws() → parseSignedClaims()</li>
 *   <li>setSubject() → subject()</li>
 *   <li>setIssuedAt() → issuedAt()</li>
 *   <li>setExpiration() → expiration()</li>
 * </ul>
 * </p>
 */
@Component
public class JwtTokenProvider {

    /** JWT 서명 비밀 키 (256비트 이상 권장) */
    private final SecretKey secretKey;

    /** 액세스 토큰 유효기간: 1시간 (밀리초) */
    private static final long ACCESS_TOKEN_EXPIRATION_MS = 60 * 60 * 1000L;

    /** 리프레시 토큰 유효기간: 7일 (밀리초) */
    private static final long REFRESH_TOKEN_EXPIRATION_MS = 7 * 24 * 60 * 60 * 1000L;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret) {
        // HMAC-SHA256 서명 키 생성 (비밀 키는 최소 256비트/32바이트 필요)
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 이메일을 subject로 하는 액세스 토큰을 생성한다.
     *
     * @param email 사용자 이메일 (토큰 subject)
     * @return 서명된 JWT 액세스 토큰 문자열
     */
    public String generateAccessToken(String email) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + ACCESS_TOKEN_EXPIRATION_MS);

        return Jwts.builder()
            .subject(email)             // 토큰 주체 (사용자 식별자)
            .issuedAt(now)              // 발급 시각
            .expiration(expiration)     // 만료 시각
            .claim("type", "access")    // 토큰 유형 구분용 커스텀 클레임
            .signWith(secretKey)        // HMAC-SHA256 서명
            .compact();
    }

    /**
     * 리프레시 토큰을 생성한다.
     * 액세스 토큰 만료 시 새 액세스 토큰 발급에 사용한다.
     *
     * @param email 사용자 이메일
     * @return 서명된 JWT 리프레시 토큰 문자열
     */
    public String generateRefreshToken(String email) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + REFRESH_TOKEN_EXPIRATION_MS);

        return Jwts.builder()
            .subject(email)
            .issuedAt(now)
            .expiration(expiration)
            .claim("type", "refresh")
            .signWith(secretKey)
            .compact();
    }

    /**
     * 토큰에서 사용자 이메일(subject)을 추출한다.
     * 토큰이 유효하지 않으면 JwtException이 발생한다.
     *
     * @param token 검증할 JWT 토큰
     * @return 토큰 subject (사용자 이메일)
     * @throws JwtException 토큰이 유효하지 않은 경우 (만료, 서명 불일치 등)
     */
    public String getEmailFromToken(String token) {
        // jjwt 0.12.x: parseSignedClaims()로 서명 검증 + 클레임 파싱
        Claims claims = Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();

        return claims.getSubject();
    }

    /**
     * 토큰의 유효성을 검증한다.
     * 만료되거나 서명이 잘못된 경우 false를 반환한다.
     *
     * @param token 검증할 JWT 토큰
     * @return 유효하면 true, 아니면 false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // 만료, 서명 불일치, 잘못된 형식 등 모든 JWT 예외 처리
            return false;
        }
    }
}
