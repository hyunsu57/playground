package com.springjpatest.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 채팅방 정보 DTO.
 *
 * <p>채팅방 목록 조회 API 응답에 사용된다.
 * 현재는 인메모리로 관리하므로 서비스 재시작 시 초기화된다.
 * </p>
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom {

    /** 채팅방 고유 ID */
    private String roomId;

    /** 채팅방 이름 */
    private String roomName;

    /** 현재 접속자 수 */
    private int userCount;
}
