import { useState } from 'react'
import { Link } from 'react-router-dom'
import useAuth from '@/hooks/useAuth.js'

/**
 * 로그인 페이지 컴포넌트.
 *
 * 이메일/비밀번호를 입력받아 로그인 처리 후 홈으로 이동한다.
 * 에러 발생 시 에러 메시지를 화면에 표시한다.
 */
const LoginPage = () => {
  // 폼 입력 상태
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  const { login } = useAuth()

  /**
   * 로그인 폼 제출 핸들러.
   * 성공 시 useAuth의 login이 홈으로 리다이렉트한다.
   */
  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setIsSubmitting(true)

    try {
      await login({ email, password })
    } catch (err) {
      // 서버 에러 메시지 또는 기본 메시지 표시
      setError(err.response?.data?.message || '이메일 또는 비밀번호가 올바르지 않습니다.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div style={{ maxWidth: '400px', margin: '4rem auto', padding: '2rem' }}>
      <h2 style={{ marginBottom: '1.5rem' }}>로그인</h2>

      <form onSubmit={handleSubmit}>
        <div style={{ marginBottom: '1rem' }}>
          <label htmlFor="email" style={{ display: 'block', marginBottom: '0.3rem' }}>이메일</label>
          <input
            id="email"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            style={{ width: '100%', padding: '0.5rem', boxSizing: 'border-box' }}
          />
        </div>

        <div style={{ marginBottom: '1rem' }}>
          <label htmlFor="password" style={{ display: 'block', marginBottom: '0.3rem' }}>비밀번호</label>
          <input
            id="password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            style={{ width: '100%', padding: '0.5rem', boxSizing: 'border-box' }}
          />
        </div>

        {/* 에러 메시지 표시 */}
        {error && (
          <p style={{ color: 'red', marginBottom: '1rem' }}>{error}</p>
        )}

        <button
          type="submit"
          disabled={isSubmitting}
          style={{ width: '100%', padding: '0.75rem', cursor: isSubmitting ? 'not-allowed' : 'pointer' }}
        >
          {isSubmitting ? '로그인 중...' : '로그인'}
        </button>
      </form>

      <p style={{ marginTop: '1rem', textAlign: 'center' }}>
        계정이 없으신가요? <Link to="/signup">회원가입</Link>
      </p>
    </div>
  )
}

export default LoginPage
