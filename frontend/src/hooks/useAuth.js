import { useState, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import { login as loginApi, signup as signupApi } from '@/api/authApi.js'

/**
 * 인증 상태 관리 커스텀 훅.
 *
 * localStorage에서 토큰 존재 여부로 로그인 상태를 판단한다.
 * 로그인/로그아웃/회원가입 액션을 제공한다.
 *
 * @returns {{
 *   isLoggedIn: boolean,
 *   login: (data: {email: string, password: string}) => Promise<void>,
 *   logout: () => void,
 *   signup: (data: {email: string, password: string, nickname: string}) => Promise<void>
 * }}
 */
const useAuth = () => {
  // localStorage에 accessToken이 있으면 로그인 상태로 초기화
  const [isLoggedIn, setIsLoggedIn] = useState(
    () => Boolean(localStorage.getItem('accessToken'))
  )
  const navigate = useNavigate()

  /**
   * 로그인 처리: 토큰을 localStorage에 저장하고 홈으로 이동.
   *
   * @param {{email: string, password: string}} data - 로그인 데이터
   */
  const login = useCallback(async (data) => {
    const tokenResponse = await loginApi(data)
    localStorage.setItem('accessToken', tokenResponse.accessToken)
    localStorage.setItem('refreshToken', tokenResponse.refreshToken)
    setIsLoggedIn(true)
    navigate('/')
  }, [navigate])

  /**
   * 로그아웃 처리: localStorage의 토큰 제거 후 로그인 페이지로 이동.
   */
  const logout = useCallback(() => {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    setIsLoggedIn(false)
    navigate('/login')
  }, [navigate])

  /**
   * 회원가입 처리: 가입 후 로그인 페이지로 이동.
   *
   * @param {{email: string, password: string, nickname: string}} data
   */
  const signup = useCallback(async (data) => {
    await signupApi(data)
    navigate('/login')
  }, [navigate])

  return { isLoggedIn, login, logout, signup }
}

export default useAuth
