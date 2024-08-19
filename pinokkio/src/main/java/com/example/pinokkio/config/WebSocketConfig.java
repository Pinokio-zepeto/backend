package com.example.pinokkio.config;

import com.example.pinokkio.api.room.WebSocketService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final WebSocketService webSocketService;
    private final ObjectMapper objectMapper;

    public WebSocketConfig(WebSocketService webSocketService, ObjectMapper objectMapper) {
        this.webSocketService = webSocketService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new CustomWebSocketHandler(), "/ws").setAllowedOrigins("*");
    }

    private class CustomWebSocketHandler extends TextWebSocketHandler {

        @Override
        public void afterConnectionEstablished(WebSocketSession session) throws Exception {
            System.out.println("New WebSocket connection: " + session.getId());
        }

        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
            // 메시지에서 토큰을 추출하고 인증
            String payload = message.getPayload();
            Map<String, String> messageData = objectMapper.readValue(payload, Map.class);

            String type = messageData.get("type");
            if ("authentication".equals(type)) {
                String token = messageData.get("token");
                if (token != null && !token.isEmpty()) {
                    webSocketService.addSession(session, token);
                } else {
                    System.out.println("Invalid token received");
                    session.close(CloseStatus.BAD_DATA);
                }
            } else {
                // 다른 메시지 처리
                webSocketService.handleMessage(session, message);
            }
        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
            System.out.println("WebSocket connection closed: " + session.getId() + ", status: " + status);
            webSocketService.removeSession(session);
        }
    }
}
