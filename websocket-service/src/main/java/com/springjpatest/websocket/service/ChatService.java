package com.springjpatest.websocket.service;

import com.springjpatest.websocket.dto.ChatMessage;
import com.springjpatest.websocket.dto.ChatRoom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;

/**
 * 채팅 비즈니스 로직 서비스.
 *
 * <p>인메모리 방식으로 채팅방과 메시지 히스토리를 관리한다.
 * - 채팅방: ConcurrentHashMap으로 동시성 안전하게 관리
 * - 메시지 히스토리: 채팅방별로 최근 50개 메시지 보관
 * 서비스 재시작 시 모든 데이터는 초기화된다.
 * </p>
 */
@Service
public class ChatService {

    /** 최대 보관 메시지 수 (채팅방당) */
    private static final int MAX_HISTORY_SIZE = 50;

    /** 채팅방 저장소: roomId → ChatRoom */
    private final Map<String, ChatRoom> rooms = new LinkedHashMap<>();

    /** 채팅방별 메시지 히스토리: roomId → 메시지 목록 */
    private final Map<String, CopyOnWriteArrayList<ChatMessage>> messageHistory =
        new ConcurrentHashMap<>();

    /** 채팅방별 현재 접속자 수: roomId → 접속자 수 */
    private final Map<String, Integer> userCountMap = new ConcurrentHashMap<>();

    /**
     * 모든 채팅방 목록 반환.
     */
    public List<ChatRoom> getAllRooms() {
        // 접속자 수를 최신 상태로 반영하여 반환
        return rooms.values().stream()
            .map(room -> ChatRoom.builder()
                .roomId(room.getRoomId())
                .roomName(room.getRoomName())
                .userCount(userCountMap.getOrDefault(room.getRoomId(), 0))
                .build())
            .toList();
    }

    /**
     * 새 채팅방 생성.
     *
     * @param roomName 생성할 채팅방 이름
     * @return 생성된 채팅방 정보
     */
    public ChatRoom createRoom(String roomName) {
        String roomId = UUID.randomUUID().toString().substring(0, 8);
        ChatRoom room = ChatRoom.builder()
            .roomId(roomId)
            .roomName(roomName)
            .userCount(0)
            .build();
        rooms.put(roomId, room);
        messageHistory.put(roomId, new CopyOnWriteArrayList<>());
        userCountMap.put(roomId, 0);
        return room;
    }

    /**
     * 특정 채팅방 조회.
     *
     * @param roomId 채팅방 ID
     * @return 채팅방 정보 (없으면 null)
     */
    public ChatRoom getRoom(String roomId) {
        ChatRoom room = rooms.get(roomId);
        if (room == null) {
            return null;
        }
        return ChatRoom.builder()
            .roomId(room.getRoomId())
            .roomName(room.getRoomName())
            .userCount(userCountMap.getOrDefault(roomId, 0))
            .build();
    }

    /**
     * 채팅방 메시지 히스토리 조회 (최근 50개).
     *
     * @param roomId 채팅방 ID
     * @return 메시지 목록
     */
    public List<ChatMessage> getMessageHistory(String roomId) {
        List<ChatMessage> history = messageHistory.get(roomId);
        if (history == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(history);
    }

    /**
     * 메시지를 히스토리에 저장.
     *
     * <p>최대 50개를 초과하면 가장 오래된 메시지부터 삭제한다.</p>
     *
     * @param message 저장할 채팅 메시지
     */
    public void saveMessage(ChatMessage message) {
        CopyOnWriteArrayList<ChatMessage> history =
            messageHistory.computeIfAbsent(message.getRoomId(), k -> new CopyOnWriteArrayList<>());
        history.add(message);
        // 최대 보관 수 초과 시 오래된 메시지 제거
        while (history.size() > MAX_HISTORY_SIZE) {
            history.remove(0);
        }
    }

    /**
     * 채팅방 접속자 수 증가 (입장 시).
     *
     * @param roomId 채팅방 ID
     */
    public void incrementUserCount(String roomId) {
        userCountMap.merge(roomId, 1, Integer::sum);
    }

    /**
     * 채팅방 접속자 수 감소 (퇴장 시).
     *
     * @param roomId 채팅방 ID
     */
    public void decrementUserCount(String roomId) {
        userCountMap.computeIfPresent(roomId, (k, v) -> Math.max(0, v - 1));
    }
}
