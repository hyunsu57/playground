import { useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import { login as loginApi, signup as signupApi } from '@/api/authApi.js'
import { useAuthContext } from '@/context/AuthContext.jsx'

/**
 * 인증 액션(로그인, 로그아웃, 회원가입)을 제공하는 커스텀 훅.
 * 인증 상태(isLoggedIn, userEmail)는 AuthContext에서 관리한다.
 */
const useAuth = () => {
  const { isLoggedIn, userEmail, saveTokens, clearTokens } = useAuthContext()
  const navigate = useNavigate()

  const login = useCallback(async (data) => {
    const tokenResponse = await loginApi(data)
    saveTokens(tokenResponse.accessToken, tokenResponse.refreshToken)
    navigate('/')
  }, [saveTokens, navigate])

  const logout = useCallback(() => {
    clearTokens()
    navigate('/login')
  }, [clearTokens, navigate])

  const signup = useCallback(async (data) => {
    await signupApi(data)
    navigate('/login')
  }, [navigate])

  return { isLoggedIn, userEmail, login, logout, signup }
}

export default useAuth
