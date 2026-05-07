package com.springjpatest.websocket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * STOMP 기반 WebSocket 메시지 브로커 설정.
 *
 * <p>클라이언트 연결 흐름:
 * 1. 클라이언트가 /ws 엔드포인트에 SockJS로 연결 (HTTP → WebSocket 업그레이드)
 * 2. /app/** 경로로 메시지 발행 → 서버 @MessageMapping 메서드 처리
 * 3. 처리 결과를 /topic/** 경로로 브로드캐스트 → 구독 중인 모든 클라이언트에게 전달
 * </p>
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 메시지 브로커 설정.
     *
     * <p>
     * - enableSimpleBroker("/topic"): 인메모리 메시지 브로커 활성화.
     *   /topic/chat.{roomId} 채널을 구독한 클라이언트에게 메시지 전달.
     * - setApplicationDestinationPrefixes("/app"): 클라이언트 → 서버 메시지 경로 접두사.
     *   /app/chat.send 로 전송하면 @MessageMapping("/chat.send") 메서드가 처리.
     * </p>
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 인메모리 브로커 활성화: 클라이언트가 /topic/** 을 구독하면 메시지 수신
        registry.enableSimpleBroker("/topic");

        // 클라이언트에서 서버로 메시지를 보낼 때 사용하는 경로 접두사
        registry.setApplicationDestinationPrefixes("/app");
    }

    /**
     * STOMP 엔드포인트 등록.
     *
     * <p>/ws 경로로 WebSocket 연결을 수락한다.
     * SockJS 폴백 지원으로 WebSocket을 지원하지 않는 환경에서도 동작 가능.
     * </p>
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
            // 프론트엔드 개발 서버(3000)와 게이트웨이(8080)에서의 연결 허용
            .setAllowedOriginPatterns("*")
            // SockJS 폴백: WebSocket 미지원 브라우저에서 Long Polling 등으로 대체
            .withSockJS();
    }
}
