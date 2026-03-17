import { Routes, Route } from 'react-router-dom'
import HomePage from '@/pages/HomePage.jsx'
import PostDetailPage from '@/pages/PostDetailPage.jsx'
import LoginPage from '@/pages/LoginPage.jsx'
import SignupPage from '@/pages/SignupPage.jsx'

/**
 * 앱 루트 컴포넌트.
 *
 * react-router-dom v7의 Routes/Route를 사용하여
 * 페이지별 라우팅을 정의한다.
 *
 * 라우트 구조:
 *   /            → 게시글 목록 (홈)
 *   /posts/:id   → 게시글 상세
 *   /login       → 로그인
 *   /signup      → 회원가입
 */
const App = () => {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/posts/:id" element={<PostDetailPage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/signup" element={<SignupPage />} />
    </Routes>
  )
}

export default App
