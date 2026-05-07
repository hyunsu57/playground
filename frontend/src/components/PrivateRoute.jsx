import { Navigate, Outlet } from 'react-router-dom'
import { useAuthContext } from '@/context/AuthContext.jsx'

/**
 * 인증이 필요한 라우트를 보호하는 컴포넌트.
 * 로그인 상태가 아니면 /login으로 리다이렉트한다.
 */
const PrivateRoute = () => {
  const { isLoggedIn } = useAuthContext()
  return isLoggedIn ? <Outlet /> : <Navigate to="/login" replace />
}

export default PrivateRoute
