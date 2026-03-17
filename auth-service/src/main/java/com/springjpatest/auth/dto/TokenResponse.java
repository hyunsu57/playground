package com.springjpatest.auth.dto;

/**
 * JWT 토큰 응답 DTO.
 *
 * <p>로그인 성공 시 액세스 토큰과 리프레시 토큰을 함께 반환한다.
 * 액세스 토큰: API 요청 시 Authorization 헤더에 사용 (유효기간 짧음)
 * 리프레시 토큰: 액세스 토큰 만료 시 재발급에 사용 (유효기간 김)
 * </p>
 *
 * @param accessToken  API 인증용 JWT 액세스 토큰
 * @param refreshToken 토큰 재발급용 JWT 리프레시 토큰
 * @param tokenType    토큰 타입 (항상 "Bearer")
 */
public record TokenResponse(
    String accessToken,
    String refreshToken,
    String tokenType
) {

    /**
     * Bearer 타입 토큰 응답을 생성하는 팩토리 메서드.
     *
     * @param accessToken  액세스 토큰
     * @param refreshToken 리프레시 토큰
     * @return TokenResponse 인스턴스
     */
    public static TokenResponse of(String accessToken, String refreshToken) {
        return new TokenResponse(accessToken, refreshToken, "Bearer");
    }
}
