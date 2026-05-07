import { useEffect, useRef, useCallback } from 'react'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

/**
 * STOMP over SockJS WebSocket 연결을 관리하는 커스텀 훅.
 *
 * 마운트 시 채팅방에 입장하고, 언마운트 시 퇴장 메시지를 보낸 후 연결 해제.
 *
 * @param {{ roomId: string, sender: string, onMessage: (msg: ChatMessage) => void }} options
 * @returns {{ sendMessage: (content: string) => void, connected: boolean }}
 */
const useWebSocket = ({ roomId, sender, onMessage }) => {
  const clientRef = useRef(null)
  // onMessage가 매 렌더마다 새 참조가 생성되어도 최신 함수를 호출하기 위한 ref
  const onMessageRef = useRef(onMessage)
  useEffect(() => {
    onMessageRef.current = onMessage
  }, [onMessage])

  useEffect(() => {
    if (!roomId || !sender) return

    const client = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      reconnectDelay: 5000,
      onConnect: () => {
        // 채팅방 토픽 구독
        client.subscribe(`/topic/chat.${roomId}`, (frame) => {
          try {
            onMessageRef.current(JSON.parse(frame.body))
          } catch { /* JSON 파싱 실패 무시 */ }
        })

        // 입장 메시지 전송
        client.publish({
          destination: '/app/chat.enter',
          body: JSON.stringify({ roomId, sender, type: 'ENTER' }),
        })
      },
    })

    client.activate()
    clientRef.current = client

    return () => {
      // 퇴장 메시지 전송 후 연결 해제
      if (clientRef.current?.connected) {
        clientRef.current.publish({
          destination: '/app/chat.leave',
          body: JSON.stringify({ roomId, sender, type: 'LEAVE' }),
        })
      }
      client.deactivate()
      clientRef.current = null
    }
  }, [roomId, sender])

  const sendMessage = useCallback((content) => {
    if (!clientRef.current?.connected) return
    clientRef.current.publish({
      destination: '/app/chat.send',
      body: JSON.stringify({ roomId, sender, content, type: 'TALK' }),
    })
  }, [roomId, sender])

  return { sendMessage }
}

export default useWebSocket
