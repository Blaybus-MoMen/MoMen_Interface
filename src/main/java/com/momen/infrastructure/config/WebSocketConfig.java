package com.momen.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket 설정
 * 특강 모듈 상태 실시간 동기화를 위한 STOMP over WebSocket 설정
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Simple in-memory message broker
        // 클라이언트가 구독할 prefix: /topic
        config.enableSimpleBroker("/topic");

        // 클라이언트가 메시지를 전송할 prefix: /app
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 엔드포인트: /ws
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")  // CORS 설정 (프론트엔드 도메인)
                .withSockJS();  // SockJS fallback 지원
    }
}
