import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getPost, createPost, updatePost } from '@/api/postApi.js'

/**
 * 게시글 작성/수정 페이지.
 * /posts/write  → 새 글 작성
 * /posts/:id/edit → 기존 글 수정 (id가 있으면 기존 데이터를 불러온다)
 */
const WritePostPage = () => {
  const { id } = useParams()
  const isEditMode = Boolean(id)
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const [title, setTitle] = useState('')
  const [summary, setSummary] = useState('')
  const [content, setContent] = useState('')
  const [published, setPublished] = useState(true)

  // 수정 모드일 때 기존 데이터 로드
  const { data: existingPost } = useQuery({
    queryKey: ['post', id],
    queryFn: () => getPost(id),
    enabled: isEditMode,
  })

  useEffect(() => {
    if (existingPost) {
      setTitle(existingPost.title)
      setSummary(existingPost.summary ?? '')
      setContent(existingPost.content)
      setPublished(existingPost.published)
    }
  }, [existingPost])

  const mutation = useMutation({
    mutationFn: (data) => isEditMode ? updatePost(id, data) : createPost(data),
    onSuccess: (savedPost) => {
      queryClient.invalidateQueries({ queryKey: ['posts'] })
      if (isEditMode) {
        queryClient.invalidateQueries({ queryKey: ['post', id] })
      }
      navigate(`/posts/${savedPost.id}`)
    },
  })

  const handleSubmit = (e) => {
    e.preventDefault()
    mutation.mutate({ title, summary, content, published })
  }

  return (
    <div className="max-w-3xl mx-auto">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">
        {isEditMode ? '게시글 수정' : '새 게시글 작성'}
      </h1>

      <form onSubmit={handleSubmit} className="flex flex-col gap-4">
        {/* 제목 */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">제목</label>
          <input
            type="text"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            required
            maxLength={200}
            placeholder="제목을 입력하세요"
            className="w-full px-3 py-2 border border-gray-300 rounded-md text-lg font-medium focus:outline-none focus:ring-2 focus:ring-indigo-500"
          />
        </div>

        {/* 요약 */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            요약 <span className="text-gray-400 font-normal">(선택)</span>
          </label>
          <input
            type="text"
            value={summary}
            onChange={(e) => setSummary(e.target.value)}
            maxLength={500}
            placeholder="목록에 표시될 짧은 요약"
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
          />
        </div>

        {/* 본문 (마크다운) */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            본문 <span className="text-gray-400 font-normal">(마크다운 지원)</span>
          </label>
          <textarea
            value={content}
            onChange={(e) => setContent(e.target.value)}
            required
            rows={20}
            placeholder="마크다운 형식으로 내용을 작성하세요..."
            className="w-full px-3 py-2 border border-gray-300 rounded-md font-mono text-sm leading-relaxed focus:outline-none focus:ring-2 focus:ring-indigo-500 resize-y"
          />
        </div>

        {/* 공개 여부 */}
        <div className="flex items-center gap-2">
          <input
            id="published"
            type="checkbox"
            checked={published}
            onChange={(e) => setPublished(e.target.checked)}
            className="rounded border-gray-300 text-indigo-600 focus:ring-indigo-500"
          />
          <label htmlFor="published" className="text-sm text-gray-700 cursor-pointer">
            공개 게시 (체크 해제 시 임시 저장)
          </label>
        </div>

        {/* 에러 메시지 */}
        {mutation.isError && (
          <p className="text-sm text-red-600">
            {mutation.error?.response?.data?.message || '저장에 실패했습니다.'}
          </p>
        )}

        {/* 버튼 */}
        <div className="flex gap-2 justify-end pt-2">
          <button
            type="button"
            onClick={() => navigate(-1)}
            className="px-4 py-2 text-sm border border-gray-300 rounded-md hover:bg-gray-100 transition-colors cursor-pointer"
          >
            취소
          </button>
          <button
            type="submit"
            disabled={mutation.isPending}
            className="px-4 py-2 text-sm bg-indigo-600 text-white rounded-md hover:bg-indigo-700 transition-colors disabled:opacity-50 cursor-pointer"
          >
            {mutation.isPending ? '저장 중...' : (isEditMode ? '수정 완료' : '발행')}
          </button>
        </div>
      </form>
    </div>
  )
}

export default WritePostPage
