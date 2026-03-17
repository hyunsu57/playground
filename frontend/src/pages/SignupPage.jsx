import { useState } from 'react'
import { Link } from 'react-router-dom'
import useAuth from '@/hooks/useAuth.js'

/**
 * 회원가입 페이지 컴포넌트.
 *
 * 이메일/비밀번호/닉네임을 입력받아 회원가입 처리 후 로그인 페이지로 이동한다.
 */
const SignupPage = () => {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [nickname, setNickname] = useState('')
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  const { signup } = useAuth()

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setIsSubmitting(true)

    try {
      await signup({ email, password, nickname })
    } catch (err) {
      setError(err.response?.data?.message || '회원가입에 실패했습니다.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div style={{ maxWidth: '400px', margin: '4rem auto', padding: '2rem' }}>
      <h2 style={{ marginBottom: '1.5rem' }}>회원가입</h2>

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
          <label htmlFor="password" style={{ display: 'block', marginBottom: '0.3rem' }}>비밀번호 (8자 이상)</label>
          <input
            id="password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            minLength={8}
            required
            style={{ width: '100%', padding: '0.5rem', boxSizing: 'border-box' }}
          />
        </div>

        <div style={{ marginBottom: '1rem' }}>
          <label htmlFor="nickname" style={{ display: 'block', marginBottom: '0.3rem' }}>닉네임 (2~20자)</label>
          <input
            id="nickname"
            type="text"
            value={nickname}
            onChange={(e) => setNickname(e.target.value)}
            minLength={2}
            maxLength={20}
            required
            style={{ width: '100%', padding: '0.5rem', boxSizing: 'border-box' }}
          />
        </div>

        {error && (
          <p style={{ color: 'red', marginBottom: '1rem' }}>{error}</p>
        )}

        <button
          type="submit"
          disabled={isSubmitting}
          style={{ width: '100%', padding: '0.75rem', cursor: isSubmitting ? 'not-allowed' : 'pointer' }}
        >
          {isSubmitting ? '가입 중...' : '회원가입'}
        </button>
      </form>

      <p style={{ marginTop: '1rem', textAlign: 'center' }}>
        이미 계정이 있으신가요? <Link to="/login">로그인</Link>
      </p>
    </div>
  )
}

export default SignupPage
