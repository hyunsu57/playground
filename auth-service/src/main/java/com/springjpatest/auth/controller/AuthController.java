package com.springjpatest.auth.controller;

import com.springjpatest.auth.dto.LoginRequest;
import com.springjpatest.auth.dto.SignupRequest;
import com.springjpatest.auth.dto.TokenResponse;
import com.springjpatest.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 REST 컨트롤러.
 *
 * <p>회원가입, 로그인, 토큰 재발급 엔드포인트를 제공한다.
 * 이 컨트롤러의 모든 엔드포인트는 게이트웨이의 JWT 검증 화이트리스트에 포함되어
 * 토큰 없이도 접근 가능하다.
 * </p>
 *
 * <ul>
 *   <li>POST /api/auth/signup   → 회원가입</li>
 *   <li>POST /api/auth/login    → 로그인 (토큰 발급)</li>
 *   <li>POST /api/auth/refresh  → 토큰 재발급</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 회원가입을 처리한다.
     * 성공 시 HTTP 201 Created를 반환한다.
     *
     * @param request 회원가입 요청 데이터 (이메일, 비밀번호, 닉네임)
     * @return HTTP 201 Created
     */
    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 로그인하고 JWT 토큰 쌍을 반환한다.
     *
     * @param request 로그인 요청 데이터 (이메일, 비밀번호)
     * @return 액세스 토큰 + 리프레시 토큰
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * 리프레시 토큰으로 새 액세스 토큰을 발급한다.
     * Authorization 헤더에서 리프레시 토큰을 추출한다.
     *
     * @param authHeader Authorization 헤더 ("Bearer {refreshToken}")
     * @return 새 액세스 토큰 + 리프레시 토큰
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
        @RequestHeader("Authorization") String authHeader
    ) {
        // "Bearer " 접두사 제거하여 토큰 문자열만 추출
        String refreshToken = authHeader.substring(7);
        return ResponseEntity.ok(authService.refresh(refreshToken));
    }
}
