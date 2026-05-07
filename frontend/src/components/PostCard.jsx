import { Link } from 'react-router-dom'

/**
 * 게시글 카드 컴포넌트.
 * 홈 페이지 목록에서 게시글 하나를 카드 형태로 표시한다.
 *
 * @param {{ post: { id, title, summary, authorEmail, createdAt } }} props
 */
const PostCard = ({ post }) => (
  <article className="bg-white rounded-lg border border-gray-200 p-5 hover:shadow-md transition-shadow">
    <h2 className="text-lg font-semibold mb-2">
      <Link
        to={`/posts/${post.id}`}
        className="text-gray-900 hover:text-indigo-600 transition-colors no-underline"
      >
        {post.title}
      </Link>
    </h2>

    {post.summary && (
      <p className="text-gray-600 text-sm leading-relaxed mb-3 line-clamp-2">
        {post.summary}
      </p>
    )}

    <footer className="flex items-center gap-2 text-xs text-gray-400">
      <span>{post.authorEmail}</span>
      <span>·</span>
      <time dateTime={post.createdAt}>
        {new Date(post.createdAt).toLocaleDateString('ko-KR')}
      </time>
    </footer>
  </article>
)

export default PostCard
