package com.springjpatest.websocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 웹소켓 서비스 진입점.
 *
 * <p>STOMP 프로토콜 기반 실시간 채팅 기능을 제공하는 독립 마이크로서비스.
 * 게이트웨이(8080)를 통해 /ws/**, /api/chat/** 요청을 처리한다.
 * </p>
 */
@SpringBootApplication
public class WebsocketServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebsocketServiceApplication.class, args);
    }
}
