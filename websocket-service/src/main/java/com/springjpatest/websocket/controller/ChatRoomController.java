package com.springjpatest.websocket.controller;

import com.springjpatest.websocket.dto.ChatMessage;
import com.springjpatest.websocket.dto.ChatRoom;
import com.springjpatest.websocket.service.ChatService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 채팅방 REST API 컨트롤러.
 *
 * <p>채팅방 목록 조회, 생성, 메시지 히스토리 조회 기능을 HTTP REST API로 제공한다.
 * WebSocket 연결 전에 채팅방 정보를 가져오는 용도로 사용된다.
 * </p>
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatService chatService;

    /**
     * 모든 채팅방 목록 조회.
     *
     * <p>GET /api/chat/rooms</p>
     *
     * @return 채팅방 목록
     */
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoom>> getRooms() {
        return ResponseEntity.ok(chatService.getAllRooms());
    }

    /**
     * 새 채팅방 생성.
     *
     * <p>POST /api/chat/rooms
     * Request Body: {"roomName": "채팅방 이름"}
     * </p>
     *
     * @param body 요청 바디 (roomName 필드)
     * @return 생성된 채팅방 정보
     */
    @PostMapping("/rooms")
    public ResponseEntity<ChatRoom> createRoom(@RequestBody Map<String, String> body) {
        String roomName = body.get("roomName");
        if (roomName == null || roomName.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(chatService.createRoom(roomName));
    }

    /**
     * 특정 채팅방 조회.
     *
     * <p>GET /api/chat/rooms/{roomId}</p>
     *
     * @param roomId 채팅방 ID
     * @return 채팅방 정보
     */
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<ChatRoom> getRoom(@PathVariable String roomId) {
        ChatRoom room = chatService.getRoom(roomId);
        if (room == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(room);
    }

    /**
     * 채팅방 메시지 히스토리 조회 (최근 50개).
     *
     * <p>GET /api/chat/rooms/{roomId}/messages
     * 채팅방 입장 시 이전 메시지를 불러오는 용도로 사용된다.
     * </p>
     *
     * @param roomId 채팅방 ID
     * @return 메시지 목록
     */
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<List<ChatMessage>> getMessages(@PathVariable String roomId) {
        return ResponseEntity.ok(chatService.getMessageHistory(roomId));
    }
}
