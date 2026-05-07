import { useState, useEffect, useRef, useCallback } from 'react'
import { useParams, Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { getRoom, getMessages } from '@/api/chatApi.js'
import { useAuthContext } from '@/context/AuthContext.jsx'
import useWebSocket from '@/hooks/useWebSocket.js'

/**
 * 채팅방 페이지.
 * STOMP over SockJS로 실시간 채팅을 구현한다.
 * 로그인 사용자는 이메일을 닉네임으로, 비로그인 사용자는 입력받은 닉네임을 사용한다.
 */
const ChatRoomPage = () => {
  const { roomId } = useParams()
  const { userEmail } = useAuthContext()

  const [messages, setMessages] = useState([])
  const [input, setInput] = useState('')
  const [nickname, setNickname] = useState(userEmail || '')
  const [nicknameConfirmed, setNicknameConfirmed] = useState(Boolean(userEmail))
  const messagesEndRef = useRef(null)

  const { data: room } = useQuery({
    queryKey: ['chatRoom', roomId],
    queryFn: () => getRoom(roomId),
  })

  // 메시지 히스토리 로드
  useQuery({
    queryKey: ['chatMessages', roomId],
    queryFn: () => getMessages(roomId),
    enabled: nicknameConfirmed,
    onSuccess: (history) => setMessages(history),
  })

  // 새 메시지 수신 시 목록에 추가
  const handleMessage = useCallback((msg) => {
    setMessages((prev) => [...prev, msg])
  }, [])

  const { sendMessage } = useWebSocket({
    roomId: nicknameConfirmed ? roomId : null,
    sender: nickname,
    onMessage: handleMessage,
  })

  // 새 메시지가 올 때 스크롤 맨 아래로
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  const handleSend = (e) => {
    e.preventDefault()
    if (!input.trim()) return
    sendMessage(input.trim())
    setInput('')
  }

  // 닉네임 미확정 상태 (비로그인 + 닉네임 미입력)
  if (!nicknameConfirmed) {
    return (
      <div className="max-w-sm mx-auto mt-16">
        <div className="bg-white border border-gray-200 rounded-xl p-6 text-center">
          <h2 className="text-lg font-bold mb-2">채팅방 입장</h2>
          <p className="text-sm text-gray-600 mb-4">
            사용할 닉네임을 입력하세요
          </p>
          <form
            onSubmit={(e) => {
              e.preventDefault()
              if (nickname.trim()) setNicknameConfirmed(true)
            }}
            className="flex flex-col gap-3"
          >
            <input
              type="text"
              value={nickname}
              onChange={(e) => setNickname(e.target.value)}
              placeholder="닉네임"
              autoFocus
              maxLength={20}
              required
              className="px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
            <button
              type="submit"
              className="py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700 transition-colors cursor-pointer"
            >
              입장
            </button>
          </form>
        </div>
      </div>
    )
  }

  return (
    <div className="flex flex-col h-[calc(100vh-8rem)]">
      {/* 채팅방 헤더 */}
      <div className="flex items-center gap-3 pb-4 border-b border-gray-200 mb-4">
        <Link to="/chat" className="text-sm text-indigo-600 hover:underline no-underline">← 목록</Link>
        <div>
          <h2 className="font-bold text-gray-900">{room?.roomName ?? '채팅방'}</h2>
          <p className="text-xs text-gray-400">{nickname}으로 입장 중 · {room?.userCount ?? 0}명 접속</p>
        </div>
      </div>

      {/* 메시지 목록 */}
      <div className="flex-1 overflow-y-auto flex flex-col gap-2 px-1">
        {messages.map((msg, idx) => (
          <MessageBubble key={idx} msg={msg} myNickname={nickname} />
        ))}
        <div ref={messagesEndRef} />
      </div>

      {/* 입력창 */}
      <form onSubmit={handleSend} className="flex gap-2 pt-4 border-t border-gray-200 mt-4">
        <input
          type="text"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder="메시지를 입력하세요..."
          className="flex-1 px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500 text-sm"
        />
        <button
          type="submit"
          disabled={!input.trim()}
          className="px-4 py-2 bg-indigo-600 text-white text-sm rounded-md hover:bg-indigo-700 transition-colors disabled:opacity-40 cursor-pointer"
        >
          전송
        </button>
      </form>
    </div>
  )
}

/**
 * 메시지 버블 컴포넌트.
 * ENTER/LEAVE 타입은 가운데 시스템 메시지, TALK은 좌/우 말풍선으로 표시한다.
 */
const MessageBubble = ({ msg, myNickname }) => {
  if (msg.type === 'ENTER' || msg.type === 'LEAVE') {
    return (
      <div className="text-center text-xs text-gray-400 py-1">
        {msg.type === 'ENTER' ? `${msg.sender} 님이 입장하셨습니다` : `${msg.sender} 님이 퇴장하셨습니다`}
      </div>
    )
  }

  const isMe = msg.sender === myNickname

  return (
    <div className={`flex flex-col ${isMe ? 'items-end' : 'items-start'}`}>
      {!isMe && <span className="text-xs text-gray-500 mb-0.5 ml-1">{msg.sender}</span>}
      <div
        className={`max-w-xs px-3 py-2 rounded-2xl text-sm leading-relaxed ${
          isMe
            ? 'bg-indigo-600 text-white rounded-br-sm'
            : 'bg-white border border-gray-200 text-gray-900 rounded-bl-sm'
        }`}
      >
        {msg.content}
      </div>
      {msg.sentAt && (
        <span className="text-xs text-gray-400 mt-0.5 mx-1">
          {new Date(msg.sentAt).toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })}
        </span>
      )}
    </div>
  )
}

export default ChatRoomPage
