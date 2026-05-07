import { createContext, useContext, useState, useCallback } from 'react'

const AuthContext = createContext(null)

/**
 * JWT 페이로드의 sub(subject) 필드에서 이메일 추출.
 * 클라이언트 측 디코딩이므로 서명 검증은 하지 않음 (UI 표시 전용).
 */
const getEmailFromToken = (token) => {
  try {
    return JSON.parse(atob(token.split('.')[1])).sub ?? null
  } catch {
    return null
  }
}

export const AuthProvider = ({ children }) => {
  const [accessToken, setAccessToken] = useState(
    () => localStorage.getItem('accessToken') || null
  )

  const isLoggedIn = Boolean(accessToken)
  const userEmail = accessToken ? getEmailFromToken(accessToken) : null

  /**
   * 로그인 성공 시 토큰을 localStorage와 state에 저장.
   */
  const saveTokens = useCallback((newAccessToken, newRefreshToken) => {
    localStorage.setItem('accessToken', newAccessToken)
    localStorage.setItem('refreshToken', newRefreshToken)
    setAccessToken(newAccessToken)
  }, [])

  /**
   * 로그아웃 시 토큰 제거.
   */
  const clearTokens = useCallback(() => {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    setAccessToken(null)
  }, [])

  return (
    <AuthContext.Provider value={{ isLoggedIn, userEmail, saveTokens, clearTokens }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuthContext = () => {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuthContext는 AuthProvider 내부에서 사용해야 합니다.')
  return ctx
}
