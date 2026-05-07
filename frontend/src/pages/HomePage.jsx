import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { getPosts } from '@/api/postApi.js'
import PostCard from '@/components/PostCard.jsx'

/**
 * 블로그 홈 페이지.
 * 공개 게시글 목록을 페이지네이션으로 표시한다.
 */
const HomePage = () => {
  const [currentPage, setCurrentPage] = useState(0)

  const { data, isLoading, isError } = useQuery({
    queryKey: ['posts', currentPage],
    queryFn: () => getPosts(currentPage, 10),
  })

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
        <div className="text-red-500">게시글을 불러오는데 실패했습니다.</div>
      </div>
    )
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-900">기술 블로그</h1>
        <span className="text-sm text-gray-500">
          총 {data?.totalElements ?? 0}개
        </span>
      </div>

      {data?.content?.length === 0 ? (
        <div className="text-center py-16 text-gray-500">
          아직 작성된 게시글이 없습니다.
        </div>
      ) : (
        <div className="flex flex-col gap-3">
          {data?.content?.map((post) => (
            <PostCard key={post.id} post={post} />
          ))}
        </div>
      )}

      {/* 페이지네이션 */}
      {data?.totalPages > 1 && (
        <div className="flex justify-center gap-1 mt-8">
          <button
            onClick={() => setCurrentPage((p) => Math.max(0, p - 1))}
            disabled={currentPage === 0}
            className="px-3 py-1.5 text-sm rounded border border-gray-300 disabled:opacity-40 hover:bg-gray-100 transition-colors cursor-pointer"
          >
            이전
          </button>

          {Array.from({ length: data.totalPages }, (_, i) => (
            <button
              key={i}
              onClick={() => setCurrentPage(i)}
              className={`px-3 py-1.5 text-sm rounded border transition-colors cursor-pointer ${
                currentPage === i
                  ? 'bg-indigo-600 text-white border-indigo-600'
                  : 'border-gray-300 hover:bg-gray-100'
              }`}
            >
              {i + 1}
            </button>
          ))}

          <button
            onClick={() => setCurrentPage((p) => Math.min(data.totalPages - 1, p + 1))}
            disabled={currentPage === data.totalPages - 1}
            className="px-3 py-1.5 text-sm rounded border border-gray-300 disabled:opacity-40 hover:bg-gray-100 transition-colors cursor-pointer"
          >
            다음
          </button>
        </div>
      )}
    </div>
  )
}

export default HomePage
