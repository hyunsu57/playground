import { Outlet } from 'react-router-dom'
import Navbar from './Navbar.jsx'

/**
 * 공통 레이아웃 컴포넌트.
 * Navbar + 콘텐츠 영역으로 구성.
 * React Router의 Outlet으로 하위 라우트를 렌더링한다.
 */
const Layout = () => (
  <div className="min-h-screen bg-gray-50">
    <Navbar />
    <main className="max-w-4xl mx-auto px-4 sm:px-6 py-8">
      <Outlet />
    </main>
  </div>
)

export default Layout
