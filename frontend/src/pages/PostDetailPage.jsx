import { useParams, Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { getPost } from '@/api/postApi.js'

/**
 * 게시글 상세 페이지 컴포넌트.
 *
 * URL 파라미터에서 게시글 ID를 추출하여 상세 내용을 표시한다.
 * 본문은 마크다운 형식이므로 추후 마크다운 렌더러를 추가할 예정이다.
 */
const PostDetailPage = () => {
  // URL 파라미터에서 게시글 ID 추출 (/posts/:id)
  const { id } = useParams()

  const { data: post, isLoading, isError } = useQuery({
    queryKey: ['post', id],
    queryFn: () => getPost(id),
  })

  if (isLoading) {
    return <div style={{ textAlign: 'center', padding: '2rem' }}>로딩 중...</div>
  }

  if (isError) {
    return <div style={{ textAlign: 'center', padding: '2rem', color: 'red' }}>게시글을 찾을 수 없습니다.</div>
  }

  return (
    <div style={{ maxWidth: '800px', margin: '0 auto', padding: '2rem' }}>
      {/* 뒤로가기 링크 */}
      <Link to="/" style={{ display: 'inline-block', marginBottom: '1rem' }}>
        ← 목록으로
      </Link>

      {/* 게시글 헤더 */}
      <header style={{ marginBottom: '2rem', borderBottom: '1px solid #eee', paddingBottom: '1rem' }}>
        <h1>{post.title}</h1>
        <p style={{ color: '#666', fontSize: '0.9rem' }}>
          작성자: {post.authorEmail} &nbsp;|&nbsp;
          작성일: {new Date(post.createdAt).toLocaleDateString('ko-KR')}
        </p>
      </header>

      {/* 게시글 본문 (추후 마크다운 렌더러 적용 예정) */}
      <article style={{ lineHeight: '1.8', whiteSpace: 'pre-wrap' }}>
        {post.content}
      </article>
    </div>
  )
}

export default PostDetailPage
