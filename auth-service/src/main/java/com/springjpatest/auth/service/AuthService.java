package com.springjpatest.auth.service;

import com.springjpatest.auth.dto.LoginRequest;
import com.springjpatest.auth.dto.SignupRequest;
import com.springjpatest.auth.dto.TokenResponse;
import com.springjpatest.auth.entity.User;
import com.springjpatest.auth.repository.UserRepository;
import com.springjpatest.auth.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 비즈니스 로직 서비스.
 *
 * <p>회원가입, 로그인, 토큰 재발급을 처리한다.
 * 비밀번호는 반드시 BCrypt로 암호화하여 저장하고 비교한다.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 신규 사용자를 등록한다.
     * 이메일 중복 시 예외를 발생시키고, 비밀번호는 BCrypt로 암호화하여 저장한다.
     *
     * @param request 회원가입 요청 DTO
     * @throws IllegalArgumentException 이메일이 이미 사용 중인 경우
     */
    @Transactional
    public void signup(SignupRequest request) {
        // 이메일 중복 검사 (이메일은 로그인 ID이므로 유일해야 함)
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다: " + request.email());
        }

        User user = User.builder()
            .email(request.email())
            .password(passwordEncoder.encode(request.password())) // BCrypt 암호화 저장
            .nickname(request.nickname())
            .build();

        userRepository.save(user);
    }

    /**
     * 로그인하고 JWT 토큰 쌍을 발급한다.
     *
     * @param request 로그인 요청 DTO
     * @return 액세스 토큰 + 리프레시 토큰
     * @throws IllegalArgumentException 이메일 미존재 또는 비밀번호 불일치
     */
    public TokenResponse login(LoginRequest request) {
        // 이메일로 사용자 조회
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

        // BCrypt 비밀번호 비교 (평문 입력 vs 저장된 해시)
        // 보안을 위해 이메일 미존재와 비밀번호 불일치를 동일한 메시지로 반환
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        // JWT 액세스/리프레시 토큰 발급
        String accessToken = jwtTokenProvider.generateAccessToken(user.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());

        return TokenResponse.of(accessToken, refreshToken);
    }

    /**
     * 리프레시 토큰으로 새 액세스 토큰을 발급한다.
     *
     * @param refreshToken 클라이언트가 보낸 리프레시 토큰
     * @return 새로운 액세스 토큰 + 리프레시 토큰
     * @throws IllegalArgumentException 리프레시 토큰이 유효하지 않은 경우
     */
    public TokenResponse refresh(String refreshToken) {
        // 리프레시 토큰 유효성 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }

        String email = jwtTokenProvider.getEmailFromToken(refreshToken);

        // 사용자가 여전히 존재하는지 확인 (계정 삭제 등의 경우 토큰 무효화)
        userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 새 토큰 쌍 발급 (리프레시 토큰도 갱신하여 보안 강화)
        String newAccessToken = jwtTokenProvider.generateAccessToken(email);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(email);

        return TokenResponse.of(newAccessToken, newRefreshToken);
    }
}
