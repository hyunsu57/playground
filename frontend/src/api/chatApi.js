import axiosClient from './axiosClient.js'

/**
 * 채팅방 목록 조회.
 *
 * @returns {Promise<ChatRoom[]>}
 */
export const getRooms = () =>
  axiosClient.get('/chat/rooms').then((res) => res.data)

/**
 * 채팅방 생성.
 *
 * @param {string} roomName
 * @returns {Promise<ChatRoom>}
 */
export const createRoom = (roomName) =>
  axiosClient.post('/chat/rooms', { roomName }).then((res) => res.data)

/**
 * 채팅방 단건 조회.
 *
 * @param {string} roomId
 * @returns {Promise<ChatRoom>}
 */
export const getRoom = (roomId) =>
  axiosClient.get(`/chat/rooms/${roomId}`).then((res) => res.data)

/**
 * 채팅방 메시지 히스토리 조회 (최근 50개).
 *
 * @param {string} roomId
 * @returns {Promise<ChatMessage[]>}
 */
export const getMessages = (roomId) =>
  axiosClient.get(`/chat/rooms/${roomId}/messages`).then((res) => res.data)
