import { Routes, Route } from 'react-router-dom'
import Layout from '@/components/Layout.jsx'
import PrivateRoute from '@/components/PrivateRoute.jsx'
import HomePage from '@/pages/HomePage.jsx'
import PostDetailPage from '@/pages/PostDetailPage.jsx'
import WritePostPage from '@/pages/WritePostPage.jsx'
import ItemsPage from '@/pages/ItemsPage.jsx'
import ChatPage from '@/pages/ChatPage.jsx'
import ChatRoomPage from '@/pages/ChatRoomPage.jsx'
import LoginPage from '@/pages/LoginPage.jsx'
import SignupPage from '@/pages/SignupPage.jsx'

/**
 * 라우트 구조:
 *   /login, /signup           → 인증 페이지 (레이아웃 없음)
 *   / (Layout 래핑)
 *     /                       → 블로그 게시글 목록
 *     /posts/write            → 게시글 작성 (로그인 필요)
 *     /posts/:id              → 게시글 상세
 *     /posts/:id/edit         → 게시글 수정 (로그인 필요)
 *     /items                  → 아이템 목록
 *     /chat                   → 채팅방 목록
 *     /chat/:roomId           → 채팅방
 */
const App = () => (
  <Routes>
    <Route path="/login" element={<LoginPage />} />
    <Route path="/signup" element={<SignupPage />} />

    <Route element={<Layout />}>
      <Route path="/" element={<HomePage />} />
      <Route path="/posts/:id" element={<PostDetailPage />} />
      <Route path="/items" element={<ItemsPage />} />
      <Route path="/chat" element={<ChatPage />} />
      <Route path="/chat/:roomId" element={<ChatRoomPage />} />

      {/* 로그인이 필요한 라우트 */}
      <Route element={<PrivateRoute />}>
        <Route path="/posts/write" element={<WritePostPage />} />
        <Route path="/posts/:id/edit" element={<WritePostPage />} />
      </Route>
    </Route>
  </Routes>
)

export default App
