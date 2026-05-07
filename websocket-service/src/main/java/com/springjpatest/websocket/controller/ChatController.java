package com.springjpatest.websocket.controller;

import com.springjpatest.websocket.dto.ChatMessage;
import com.springjpatest.websocket.service.ChatService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

/**
 * STOMP 메시지 처리 컨트롤러.
 *
 * <p>클라이언트가 /app/** 경로로 메시지를 보내면 이 컨트롤러가 처리한다.
 * 처리 후 /topic/chat.{roomId} 로 브로드캐스트하여 해당 채팅방 구독자 전원에게 전달한다.
 * </p>
 *
 * <p>STOMP 통신 흐름:
 * 클라이언트 연결: CONNECT /ws
 * 채팅방 구독: SUBSCRIBE /topic/chat.{roomId}
 * 메시지 발송: SEND /app/chat.send
 * 입장/퇴장:   SEND /app/chat.enter, /app/chat.leave
 * </p>
 */
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatService chatService;

    /**
     * 채팅방 입장 처리.
     *
     * <p>입장 메시지를 해당 채팅방 구독자 전원에게 브로드캐스트한다.
     * 클라이언트 구독 경로: /topic/chat.{roomId}
     * </p>
     *
     * @param message 입장 메시지 (type=ENTER, roomId, sender 필드 필수)
     */
    @MessageMapping("/chat.enter")
    public void enter(@Payload ChatMessage message) {
        // 입장 안내 메시지 생성
        ChatMessage enterMessage = ChatMessage.builder()
            .type(ChatMessage.MessageType.ENTER)
            .roomId(message.getRoomId())
            .sender(message.getSender())
            .content(message.getSender() + "님이 입장하셨습니다.")
            .sentAt(LocalDateTime.now())
            .build();

        // 접속자 수 증가
        chatService.incrementUserCount(message.getRoomId());

        // 채팅방 구독자 전원에게 입장 알림 전송
        messagingTemplate.convertAndSend(
            "/topic/chat." + message.getRoomId(), enterMessage);
    }

    /**
     * 채팅 메시지 전송 처리.
     *
     * <p>클라이언트가 보낸 메시지에 발송 시각을 추가하여 브로드캐스트한다.
     * 메시지는 히스토리에 저장되어 이후 입장한 사용자도 조회 가능하다.
     * </p>
     *
     * @param message 채팅 메시지 (type=TALK, roomId, sender, content 필드 필수)
     */
    @MessageMapping("/chat.send")
    public void send(@Payload ChatMessage message) {
        // 서버 시각 추가
        ChatMessage talkMessage = ChatMessage.builder()
            .type(ChatMessage.MessageType.TALK)
            .roomId(message.getRoomId())
            .sender(message.getSender())
            .content(message.getContent())
            .sentAt(LocalDateTime.now())
            .build();

        // 메시지 히스토리 저장
        chatService.saveMessage(talkMessage);

        // 채팅방 구독자 전원에게 메시지 전송
        messagingTemplate.convertAndSend(
            "/topic/chat." + message.getRoomId(), talkMessage);
    }

    /**
     * 채팅방 퇴장 처리.
     *
     * <p>퇴장 메시지를 해당 채팅방 구독자 전원에게 브로드캐스트한다.
     * </p>
     *
     * @param message 퇴장 메시지 (type=LEAVE, roomId, sender 필드 필수)
     */
    @MessageMapping("/chat.leave")
    public void leave(@Payload ChatMessage message) {
        // 퇴장 안내 메시지 생성
        ChatMessage leaveMessage = ChatMessage.builder()
            .type(ChatMessage.MessageType.LEAVE)
            .roomId(message.getRoomId())
            .sender(message.getSender())
            .content(message.getSender() + "님이 퇴장하셨습니다.")
            .sentAt(LocalDateTime.now())
            .build();

        // 접속자 수 감소
        chatService.decrementUserCount(message.getRoomId());

        // 채팅방 구독자 전원에게 퇴장 알림 전송
        messagingTemplate.convertAndSend(
            "/topic/chat." + message.getRoomId(), leaveMessage);
    }
}
