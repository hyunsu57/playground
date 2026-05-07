import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { getRooms, createRoom } from '@/api/chatApi.js'

/**
 * 채팅방 목록 페이지.
 * 채팅방 생성 및 입장 기능을 제공한다.
 */
const ChatPage = () => {
  const queryClient = useQueryClient()
  const [roomName, setRoomName] = useState('')
  const [showInput, setShowInput] = useState(false)

  const { data: rooms, isLoading } = useQuery({
    queryKey: ['chatRooms'],
    queryFn: getRooms,
    refetchInterval: 10000, // 10초마다 자동 갱신 (접속자 수 반영)
  })

  const createMutation = useMutation({
    mutationFn: () => createRoom(roomName),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['chatRooms'] })
      setRoomName('')
      setShowInput(false)
    },
  })

  const handleCreate = (e) => {
    e.preventDefault()
    if (!roomName.trim()) return
    createMutation.mutate()
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-900">채팅</h1>
        <button
          onClick={() => setShowInput(!showInput)}
          className="px-3 py-1.5 text-sm bg-indigo-600 text-white rounded-md hover:bg-indigo-700 transition-colors cursor-pointer"
        >
          + 방 만들기
        </button>
      </div>

      {/* 방 생성 폼 */}
      {showInput && (
        <form onSubmit={handleCreate} className="flex gap-2 mb-4">
          <input
            type="text"
            value={roomName}
            onChange={(e) => setRoomName(e.target.value)}
            placeholder="채팅방 이름을 입력하세요"
            autoFocus
            className="flex-1 px-3 py-2 text-sm border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
          />
          <button
            type="submit"
            disabled={createMutation.isPending || !roomName.trim()}
            className="px-4 py-2 text-sm bg-indigo-600 text-white rounded-md hover:bg-indigo-700 disabled:opacity-50 cursor-pointer"
          >
            {createMutation.isPending ? '생성 중...' : '생성'}
          </button>
          <button
            type="button"
            onClick={() => setShowInput(false)}
            className="px-3 py-2 text-sm border border-gray-300 rounded-md hover:bg-gray-100 cursor-pointer"
          >
            취소
          </button>
        </form>
      )}

      {/* 채팅방 목록 */}
      {isLoading ? (
        <div className="text-center py-12 text-gray-500">로딩 중...</div>
      ) : rooms?.length === 0 ? (
        <div className="text-center py-12 text-gray-500">
          채팅방이 없습니다. 첫 번째 채팅방을 만들어보세요!
        </div>
      ) : (
        <div className="flex flex-col gap-2">
          {rooms?.map((room) => (
            <Link
              key={room.roomId}
              to={`/chat/${room.roomId}`}
              className="bg-white border border-gray-200 rounded-lg px-5 py-4 flex items-center justify-between hover:shadow-md hover:border-indigo-300 transition-all no-underline group"
            >
              <div>
                <p className="font-medium text-gray-900 group-hover:text-indigo-600 transition-colors">
                  {room.roomName}
                </p>
                <p className="text-xs text-gray-400 mt-0.5">#{room.roomId}</p>
              </div>
              <div className="flex items-center gap-1 text-sm text-gray-500">
                <span className="w-2 h-2 rounded-full bg-green-400 inline-block"></span>
                <span>{room.userCount}명</span>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  )
}

export default ChatPage
