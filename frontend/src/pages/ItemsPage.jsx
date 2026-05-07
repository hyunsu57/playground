import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getItems, createItem, deleteItem } from '@/api/itemApi.js'
import { useAuthContext } from '@/context/AuthContext.jsx'

// 아이템 타입 레이블 매핑
const ITEM_TYPE_LABELS = {
  ALL: '전체',
  GAME_MONEY: '게임머니',
  ITEM: '아이템',
  ACCOUNT: '계정',
  ETC: '기타',
}

// 정렬 옵션
const SORT_OPTIONS = [
  { value: 'RegDateMax', label: '최신순' },
  { value: 'RegDateMin', label: '오래된순' },
  { value: 'PriceMax', label: '낮은가격순' },
  { value: 'PriceMin', label: '높은가격순' },
]

const defaultFilters = {
  keyword: '',
  itemType: 'ALL',
  server: '',
  minPrice: '',
  maxPrice: '',
  sort: 'RegDateMax',
}

/**
 * 아이템 거래 목록 페이지.
 * 필터링, 페이지네이션, 아이템 등록 기능을 제공한다.
 */
const ItemsPage = () => {
  const { isLoggedIn } = useAuthContext()
  const queryClient = useQueryClient()

  const [filters, setFilters] = useState(defaultFilters)
  const [appliedFilters, setAppliedFilters] = useState(defaultFilters)
  const [currentPage, setCurrentPage] = useState(0)
  const [showCreateModal, setShowCreateModal] = useState(false)

  const { data, isLoading } = useQuery({
    queryKey: ['items', appliedFilters, currentPage],
    queryFn: () => getItems({
      ...appliedFilters,
      itemType: appliedFilters.itemType === 'ALL' ? undefined : appliedFilters.itemType,
      minPrice: appliedFilters.minPrice || undefined,
      maxPrice: appliedFilters.maxPrice || undefined,
      server: appliedFilters.server || undefined,
      keyword: appliedFilters.keyword || undefined,
      page: currentPage,
      size: 15,
    }),
  })

  const deleteMutation = useMutation({
    mutationFn: deleteItem,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['items'] }),
  })

  const handleSearch = (e) => {
    e.preventDefault()
    setCurrentPage(0)
    setAppliedFilters({ ...filters })
  }

  const handleReset = () => {
    setFilters(defaultFilters)
    setAppliedFilters(defaultFilters)
    setCurrentPage(0)
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-900">아이템 거래</h1>
        {isLoggedIn && (
          <button
            onClick={() => setShowCreateModal(true)}
            className="px-3 py-1.5 text-sm bg-indigo-600 text-white rounded-md hover:bg-indigo-700 transition-colors cursor-pointer"
          >
            + 아이템 등록
          </button>
        )}
      </div>

      {/* 필터 패널 */}
      <form onSubmit={handleSearch} className="bg-white border border-gray-200 rounded-lg p-4 mb-4 flex flex-col gap-3">
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
          <input
            type="text"
            placeholder="검색어"
            value={filters.keyword}
            onChange={(e) => setFilters({ ...filters, keyword: e.target.value })}
            className="px-3 py-1.5 text-sm border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-indigo-500"
          />
          <select
            value={filters.itemType}
            onChange={(e) => setFilters({ ...filters, itemType: e.target.value })}
            className="px-3 py-1.5 text-sm border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-indigo-500"
          >
            {Object.entries(ITEM_TYPE_LABELS).map(([value, label]) => (
              <option key={value} value={value}>{label}</option>
            ))}
          </select>
          <input
            type="text"
            placeholder="서버명"
            value={filters.server}
            onChange={(e) => setFilters({ ...filters, server: e.target.value })}
            className="px-3 py-1.5 text-sm border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-indigo-500"
          />
          <select
            value={filters.sort}
            onChange={(e) => setFilters({ ...filters, sort: e.target.value })}
            className="px-3 py-1.5 text-sm border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-indigo-500"
          >
            {SORT_OPTIONS.map((o) => (
              <option key={o.value} value={o.value}>{o.label}</option>
            ))}
          </select>
        </div>
        <div className="flex gap-2 items-center">
          <input
            type="number"
            placeholder="최소 가격"
            value={filters.minPrice}
            onChange={(e) => setFilters({ ...filters, minPrice: e.target.value })}
            className="w-28 px-3 py-1.5 text-sm border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-indigo-500"
          />
          <span className="text-gray-400 text-sm">~</span>
          <input
            type="number"
            placeholder="최대 가격"
            value={filters.maxPrice}
            onChange={(e) => setFilters({ ...filters, maxPrice: e.target.value })}
            className="w-28 px-3 py-1.5 text-sm border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-indigo-500"
          />
          <div className="flex gap-2 ml-auto">
            <button
              type="button"
              onClick={handleReset}
              className="px-3 py-1.5 text-sm border border-gray-300 rounded hover:bg-gray-100 transition-colors cursor-pointer"
            >
              초기화
            </button>
            <button
              type="submit"
              className="px-4 py-1.5 text-sm bg-indigo-600 text-white rounded hover:bg-indigo-700 transition-colors cursor-pointer"
            >
              검색
            </button>
          </div>
        </div>
      </form>

      {/* 아이템 테이블 */}
      {isLoading ? (
        <div className="text-center py-12 text-gray-500">로딩 중...</div>
      ) : (
        <>
          <div className="bg-white border border-gray-200 rounded-lg overflow-hidden">
            <table className="w-full text-sm">
              <thead className="bg-gray-50 text-gray-600">
                <tr>
                  <th className="px-4 py-2.5 text-left font-medium">상품명</th>
                  <th className="px-4 py-2.5 text-left font-medium hidden sm:table-cell">분류</th>
                  <th className="px-4 py-2.5 text-left font-medium hidden sm:table-cell">서버</th>
                  <th className="px-4 py-2.5 text-right font-medium">가격</th>
                  <th className="px-4 py-2.5 text-right font-medium">수량</th>
                  <th className="px-4 py-2.5 text-left font-medium hidden md:table-cell">판매자</th>
                  {isLoggedIn && <th className="px-4 py-2.5 text-center font-medium">관리</th>}
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {data?.content?.length === 0 ? (
                  <tr>
                    <td colSpan={7} className="px-4 py-8 text-center text-gray-500">
                      등록된 아이템이 없습니다.
                    </td>
                  </tr>
                ) : (
                  data?.content?.map((item) => (
                    <tr key={item.id} className="hover:bg-gray-50">
                      <td className="px-4 py-3 font-medium text-gray-900">{item.title}</td>
                      <td className="px-4 py-3 text-gray-600 hidden sm:table-cell">
                        <span className="px-2 py-0.5 bg-indigo-50 text-indigo-700 rounded text-xs">
                          {ITEM_TYPE_LABELS[item.itemType] ?? item.itemType}
                        </span>
                      </td>
                      <td className="px-4 py-3 text-gray-600 hidden sm:table-cell">{item.server}</td>
                      <td className="px-4 py-3 text-right text-gray-900 font-medium">
                        {Number(item.price).toLocaleString('ko-KR')}원
                      </td>
                      <td className="px-4 py-3 text-right text-gray-600">{item.quantity}</td>
                      <td className="px-4 py-3 text-gray-600 hidden md:table-cell">{item.sellerName}</td>
                      {isLoggedIn && (
                        <td className="px-4 py-3 text-center">
                          <button
                            onClick={() => {
                              if (window.confirm('삭제하시겠습니까?')) deleteMutation.mutate(item.id)
                            }}
                            className="text-xs text-red-500 hover:text-red-700 cursor-pointer"
                          >
                            삭제
                          </button>
                        </td>
                      )}
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>

          {/* 페이지네이션 */}
          {data?.totalPages > 1 && (
            <div className="flex justify-center gap-1 mt-6">
              <button
                onClick={() => setCurrentPage((p) => Math.max(0, p - 1))}
                disabled={currentPage === 0}
                className="px-3 py-1.5 text-sm rounded border border-gray-300 disabled:opacity-40 hover:bg-gray-100 cursor-pointer"
              >
                이전
              </button>
              {Array.from({ length: data.totalPages }, (_, i) => (
                <button
                  key={i}
                  onClick={() => setCurrentPage(i)}
                  className={`px-3 py-1.5 text-sm rounded border cursor-pointer ${
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
                className="px-3 py-1.5 text-sm rounded border border-gray-300 disabled:opacity-40 hover:bg-gray-100 cursor-pointer"
              >
                다음
              </button>
            </div>
          )}
        </>
      )}

      {/* 아이템 등록 모달 */}
      {showCreateModal && (
        <CreateItemModal
          onClose={() => setShowCreateModal(false)}
          onCreated={() => {
            queryClient.invalidateQueries({ queryKey: ['items'] })
            setShowCreateModal(false)
          }}
        />
      )}
    </div>
  )
}

/**
 * 아이템 등록 모달 컴포넌트.
 */
const CreateItemModal = ({ onClose, onCreated }) => {
  const [form, setForm] = useState({
    server: '',
    sellerName: '',
    itemType: 'ITEM',
    title: '',
    price: '',
    quantity: 1,
  })
  const [error, setError] = useState('')

  const mutation = useMutation({
    mutationFn: createItem,
    onSuccess: onCreated,
    onError: (err) => setError(err.response?.data?.message || '등록에 실패했습니다.'),
  })

  const handleSubmit = (e) => {
    e.preventDefault()
    mutation.mutate({ ...form, price: Number(form.price), quantity: Number(form.quantity) })
  }

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 px-4">
      <div className="bg-white rounded-xl p-6 w-full max-w-md">
        <h2 className="text-lg font-bold mb-4">아이템 등록</h2>
        <form onSubmit={handleSubmit} className="flex flex-col gap-3">
          {[
            { label: '서버명', key: 'server', placeholder: '예: 라엘08' },
            { label: '판매자명', key: 'sellerName', placeholder: '판매자 닉네임' },
            { label: '상품명', key: 'title', placeholder: '상품명 (최대 100자)' },
          ].map(({ label, key, placeholder }) => (
            <div key={key}>
              <label className="block text-sm font-medium text-gray-700 mb-1">{label}</label>
              <input
                type="text"
                required
                placeholder={placeholder}
                value={form[key]}
                onChange={(e) => setForm({ ...form, [key]: e.target.value })}
                className="w-full px-3 py-2 text-sm border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-indigo-500"
              />
            </div>
          ))}
          <div className="grid grid-cols-3 gap-2">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">분류</label>
              <select
                value={form.itemType}
                onChange={(e) => setForm({ ...form, itemType: e.target.value })}
                className="w-full px-2 py-2 text-sm border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-indigo-500"
              >
                {Object.entries(ITEM_TYPE_LABELS)
                  .filter(([v]) => v !== 'ALL')
                  .map(([value, label]) => (
                    <option key={value} value={value}>{label}</option>
                  ))}
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">가격</label>
              <input
                type="number"
                required
                min={0}
                value={form.price}
                onChange={(e) => setForm({ ...form, price: e.target.value })}
                className="w-full px-3 py-2 text-sm border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-indigo-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">수량</label>
              <input
                type="number"
                required
                min={1}
                value={form.quantity}
                onChange={(e) => setForm({ ...form, quantity: e.target.value })}
                className="w-full px-3 py-2 text-sm border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-indigo-500"
              />
            </div>
          </div>

          {error && <p className="text-sm text-red-600">{error}</p>}

          <div className="flex gap-2 justify-end pt-1">
            <button type="button" onClick={onClose} className="px-4 py-2 text-sm border border-gray-300 rounded hover:bg-gray-100 cursor-pointer">
              취소
            </button>
            <button
              type="submit"
              disabled={mutation.isPending}
              className="px-4 py-2 text-sm bg-indigo-600 text-white rounded hover:bg-indigo-700 disabled:opacity-50 cursor-pointer"
            >
              {mutation.isPending ? '등록 중...' : '등록'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default ItemsPage
