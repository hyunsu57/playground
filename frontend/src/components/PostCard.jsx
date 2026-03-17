import { Link } from 'react-router-dom'

/**
 * 게시글 카드 컴포넌트.
 *
 * 홈 페이지 목록에서 게시글 하나를 카드 형태로 표시한다.
 * 제목 클릭 시 상세 페이지로 이동한다.
 *
 * @param {{ post: {id: number, title: string, summary: string, authorEmail: string, createdAt: string} }} props
 */
const PostCard = ({ post }) => {
  return (
    <article
      style={{
        border: '1px solid #e0e0e0',
        borderRadius: '8px',
        padding: '1.5rem',
        marginBottom: '1rem',
        transition: 'box-shadow 0.2s',
      }}
    >
      {/* 게시글 제목 (클릭 시 상세 페이지로 이동) */}
      <h2 style={{ marginBottom: '0.5rem', fontSize: '1.25rem' }}>
        <Link
          to={`/posts/${post.id}`}
          style={{ textDecoration: 'none', color: '#1a1a1a' }}
        >
          {post.title}
        </Link>
      </h2>

      {/* 게시글 요약 */}
      {post.summary && (
        <p style={{ color: '#444', marginBottom: '0.75rem', lineHeight: '1.6' }}>
          {post.summary}
        </p>
      )}

      {/* 메타 정보: 작성자, 작성일 */}
      <footer style={{ color: '#888', fontSize: '0.85rem' }}>
        <span>{post.authorEmail}</span>
        <span style={{ margin: '0 0.5rem' }}>·</span>
        <time dateTime={post.createdAt}>
          {new Date(post.createdAt).toLocaleDateString('ko-KR')}
        </time>
      </footer>
    </article>
  )
}

export default PostCard
