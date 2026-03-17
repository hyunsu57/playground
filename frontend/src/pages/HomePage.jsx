import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { getPosts } from '@/api/postApi.js'
import PostCard from '@/components/PostCard.jsx'

/**
 * 홈 페이지 컴포넌트.
 *
 * 공개된 게시글 목록을 페이지네이션으로 보여준다.
 * React Query의 useQuery로 서버 상태를 캐싱하여 불필요한 재요청을 방지한다.
 */
const HomePage = () => {
  // 현재 페이지 번호 (0부터 시작)
  const [currentPage, setCurrentPage] = useState(0)

  // 게시글 목록 조회 (React Query - 캐싱, 로딩 상태 자동 관리)
  const { data, isLoading, isError } = useQuery({
    queryKey: ['posts', currentPage],
    queryFn: () => getPosts(currentPage, 10),
  })

  if (isLoading) {
    return <div style={{ textAlign: 'center', padding: '2rem' }}>로딩 중...</div>
  }

  if (isError) {
    return <div style={{ textAlign: 'center', padding: '2rem', color: 'red' }}>게시글을 불러오는데 실패했습니다.</div>
  }

  return (
    <div style={{ maxWidth: '800px', margin: '0 auto', padding: '2rem' }}>
      {/* 헤더 영역 */}
      <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h1>기술 블로그</h1>
        <nav>
          <Link to="/login" style={{ marginRight: '1rem' }}>로그인</Link>
          <Link to="/signup">회원가입</Link>
        </nav>
      </header>

      {/* 게시글 목록 */}
      <main>
        {data?.content?.length === 0 ? (
          <p>아직 작성된 게시글이 없습니다.</p>
        ) : (
          data?.content?.map((post) => (
            <PostCard key={post.id} post={post} />
          ))
        )}
      </main>

      {/* 페이지네이션 */}
      {data?.totalPages > 1 && (
        <footer style={{ display: 'flex', justifyContent: 'center', gap: '0.5rem', marginTop: '2rem' }}>
          {Array.from({ length: data.totalPages }, (_, i) => (
            <button
              key={i}
              onClick={() => setCurrentPage(i)}
              style={{
                padding: '0.5rem 1rem',
                fontWeight: currentPage === i ? 'bold' : 'normal',
                cursor: 'pointer',
              }}
            >
              {i + 1}
            </button>
          ))}
        </footer>
      )}
    </div>
  )
}

export default HomePage
