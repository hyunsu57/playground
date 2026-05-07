import { useParams, Link, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import ReactMarkdown from 'react-markdown'
import { getPost, deletePost } from '@/api/postApi.js'
import { useAuthContext } from '@/context/AuthContext.jsx'

/**
 * 게시글 상세 페이지.
 * 마크다운 형식의 본문을 ReactMarkdown으로 렌더링한다.
 * 작성자 본인인 경우 수정/삭제 버튼을 표시한다.
 */
const PostDetailPage = () => {
  const { id } = useParams()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { userEmail } = useAuthContext()

  const { data: post, isLoading, isError } = useQuery({
    queryKey: ['post', id],
    queryFn: () => getPost(id),
  })

  const deleteMutation = useMutation({
    mutationFn: () => deletePost(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['posts'] })
      navigate('/')
    },
  })

  const handleDelete = () => {
    if (window.confirm('정말로 삭제하시겠습니까?')) {
      deleteMutation.mutate()
    }
  }

  if (isLoading) {
    return (
      <div className="flex justify-center py-16">
        <div className="text-gray-500">로딩 중...</div>
      </div>
    )
  }

  if (isError) {
    return (
      <div className="flex justify-center py-16">
        <div className="text-red-500">게시글을 찾을 수 없습니다.</div>
      </div>
    )
  }

  const isAuthor = userEmail && post.authorEmail === userEmail

  return (
    <div>
      {/* 상단 내비게이션 */}
      <div className="flex items-center justify-between mb-6">
        <Link to="/" className="text-sm text-indigo-600 hover:underline no-underline">
          ← 목록으로
        </Link>

        {isAuthor && (
          <div className="flex gap-2">
            <Link
              to={`/posts/${id}/edit`}
              className="px-3 py-1.5 text-sm border border-gray-300 rounded hover:bg-gray-100 transition-colors no-underline text-gray-700"
            >
              수정
            </Link>
            <button
              onClick={handleDelete}
              disabled={deleteMutation.isPending}
              className="px-3 py-1.5 text-sm border border-red-300 text-red-600 rounded hover:bg-red-50 transition-colors cursor-pointer disabled:opacity-50"
            >
              {deleteMutation.isPending ? '삭제 중...' : '삭제'}
            </button>
          </div>
        )}
      </div>

      {/* 게시글 헤더 */}
      <header className="mb-8 pb-6 border-b border-gray-200">
        <h1 className="text-3xl font-bold text-gray-900 mb-3">{post.title}</h1>
        <div className="flex items-center gap-2 text-sm text-gray-500">
          <span>{post.authorEmail}</span>
          <span>·</span>
          <time dateTime={post.createdAt}>
            {new Date(post.createdAt).toLocaleDateString('ko-KR', {
              year: 'numeric', month: 'long', day: 'numeric',
            })}
          </time>
          {post.updatedAt !== post.createdAt && (
            <>
              <span>·</span>
              <span>수정됨</span>
            </>
          )}
        </div>
      </header>

      {/* 게시글 본문 (마크다운 렌더링) */}
      <article className="prose">
        <ReactMarkdown>{post.content}</ReactMarkdown>
      </article>
    </div>
  )
}

export default PostDetailPage
