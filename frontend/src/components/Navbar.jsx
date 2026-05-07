import { Link } from 'react-router-dom'
import useAuth from '@/hooks/useAuth.js'

/**
 * 상단 네비게이션 바.
 * AuthContext의 isLoggedIn 상태에 따라 로그인/로그아웃 버튼을 동적으로 렌더링한다.
 */
const Navbar = () => {
  const { isLoggedIn, userEmail, logout } = useAuth()

  return (
    <header className="bg-white border-b border-gray-200 sticky top-0 z-50">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 h-14 flex items-center justify-between">

        {/* 로고 */}
        <Link to="/" className="font-bold text-lg text-indigo-600 no-underline">
          PlayGround
        </Link>

        {/* 메인 네비게이션 */}
        <nav className="hidden sm:flex items-center gap-6 text-sm text-gray-600">
          <Link to="/" className="hover:text-indigo-600 transition-colors no-underline">블로그</Link>
          <Link to="/items" className="hover:text-indigo-600 transition-colors no-underline">아이템</Link>
          <Link to="/chat" className="hover:text-indigo-600 transition-colors no-underline">채팅</Link>
        </nav>

        {/* 인증 영역 */}
        <div className="flex items-center gap-2">
          {isLoggedIn ? (
            <>
              <span className="hidden sm:block text-sm text-gray-500 mr-1">{userEmail}</span>
              <Link
                to="/posts/write"
                className="px-3 py-1.5 text-sm bg-indigo-600 text-white rounded-md hover:bg-indigo-700 transition-colors no-underline"
              >
                글쓰기
              </Link>
              <button
                onClick={logout}
                className="px-3 py-1.5 text-sm text-gray-600 hover:text-gray-900 transition-colors cursor-pointer"
              >
                로그아웃
              </button>
            </>
          ) : (
            <>
              <Link
                to="/login"
                className="px-3 py-1.5 text-sm text-gray-600 hover:text-gray-900 transition-colors no-underline"
              >
                로그인
              </Link>
              <Link
                to="/signup"
                className="px-3 py-1.5 text-sm bg-indigo-600 text-white rounded-md hover:bg-indigo-700 transition-colors no-underline"
              >
                회원가입
              </Link>
            </>
          )}
        </div>
      </div>

      {/* 모바일 네비게이션 */}
      <nav className="sm:hidden flex border-t border-gray-100 text-sm text-gray-600">
        <Link to="/" className="flex-1 py-2 text-center hover:text-indigo-600 transition-colors no-underline">블로그</Link>
        <Link to="/items" className="flex-1 py-2 text-center hover:text-indigo-600 transition-colors no-underline">아이템</Link>
        <Link to="/chat" className="flex-1 py-2 text-center hover:text-indigo-600 transition-colors no-underline">채팅</Link>
      </nav>
    </header>
  )
}

export default Navbar
