import axiosClient from './axiosClient.js'

/**
 * 인증 관련 API 함수 모음.
 * axiosClient를 통해 auth-service API를 호출한다.
 */

/**
 * 회원가입을 요청한다.
 *
 * @param {{email: string, password: string, nickname: string}} data
 * @returns {Promise<void>}
 */
export const signup = (data) =>
  axiosClient.post('/auth/signup', data)

/**
 * 로그인하고 JWT 토큰 쌍을 반환받는다.
 *
 * @param {{email: string, password: string}} data
 * @returns {Promise<{accessToken: string, refreshToken: string, tokenType: string}>}
 */
export const login = (data) =>
  axiosClient.post('/auth/login', data).then((res) => res.data)

/**
 * 리프레시 토큰으로 새 액세스 토큰을 발급받는다.
 * axiosClient의 인터셉터에서 자동으로 호출되므로 직접 호출은 드물다.
 *
 * @param {string} refreshToken - 리프레시 토큰
 * @returns {Promise<{accessToken: string, refreshToken: string, tokenType: string}>}
 */
export const refresh = (refreshToken) =>
  axiosClient.post('/auth/refresh', null, {
    headers: { Authorization: `Bearer ${refreshToken}` },
  }).then((res) => res.data)
