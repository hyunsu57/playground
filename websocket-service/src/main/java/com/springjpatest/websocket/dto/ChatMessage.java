package com.springjpatest.websocket.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 채팅 메시지 DTO.
 *
 * <p>클라이언트 → 서버 (발신), 서버 → 클라이언트 (수신) 양방향으로 사용된다.
 * type 필드로 메시지 종류를 구분하여 클라이언트에서 입장/퇴장 알림을 표시할 수 있다.
 * </p>
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    /**
     * 메시지 유형.
     * - ENTER: 사용자 채팅방 입장
     * - TALK: 일반 채팅 메시지
     * - LEAVE: 사용자 채팅방 퇴장
     */
    public enum MessageType {
        ENTER, TALK, LEAVE
    }

    /** 메시지 유형 */
    private MessageType type;

    /** 채팅방 ID (예: "general", "room-1") */
    private String roomId;

    /** 발신자 닉네임 */
    private String sender;

    /** 메시지 내용 */
    private String content;

    /** 서버에서 설정하는 발송 시각 */
    private LocalDateTime sentAt;
}
