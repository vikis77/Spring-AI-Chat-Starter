package com.randb.springaichatstarter.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.randb.springaichatstarter.core.ChatModelFactory;
import com.randb.springaichatstarter.core.ChatService;
import com.randb.springaichatstarter.dto.ChatRequest;
import com.randb.springaichatstarter.dto.ChatResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket处理器
 * 只有在启用WebSocket时才创建
 * @Date: 2025-07-11 13:09:33
 * @Author: randb
 */
@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "spring.ai.chat.websocket", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatModelFactory chatModelFactory;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("ChatWebSocketHandler已创建，准备处理WebSocket连接");
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);
        log.info("WebSocket connection established: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            String payload = message.getPayload();
            ChatRequest request = objectMapper.readValue(payload, ChatRequest.class);
            
            if (request.getModel() == null || request.getModel().isEmpty()) {
                request.setModel("qwen"); // 默认使用通义千问
            }
            
            log.info("Received WebSocket message: {}", payload);
            
            ChatService chatService = chatModelFactory.get(request.getModel());
            
            chatService.streamReply(request).subscribe(
                content -> sendMessage(session, createResponse(request, content)),
                error -> handleError(session, error),
                () -> log.info("WebSocket streaming completed for session: {}", session.getId())
            );
        } catch (Exception e) {
            log.error("Error handling WebSocket message", e);
            sendErrorMessage(session, "Error processing request: " + e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
        log.info("WebSocket connection closed: {}", session.getId());
    }

    private void sendMessage(WebSocketSession session, String message) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(message));
            }
        } catch (IOException e) {
            log.error("Error sending WebSocket message", e);
        }
    }

    private void handleError(WebSocketSession session, Throwable error) {
        log.error("Error in WebSocket stream", error);
        try {
            String errorMessage = "Error in AI response: " + (error.getMessage() != null ? error.getMessage() : "Connection reset");
            sendErrorMessage(session, errorMessage);
        } catch (Exception e) {
            log.error("Failed to send error message", e);
        }
    }

    private void sendErrorMessage(WebSocketSession session, String errorMessage) {
        try {
            ChatResponse errorResponse = new ChatResponse();
            errorResponse.setContent(errorMessage);
            errorResponse.setRequestId(UUID.randomUUID().toString());
            String json = objectMapper.writeValueAsString(errorResponse);
            sendMessage(session, json);
        } catch (Exception e) {
            log.error("Error sending error message", e);
        }
    }

    private String createResponse(ChatRequest request, String content) {
        try {
            ChatResponse response = new ChatResponse();
            response.setContent(content);
            response.setUserId(request.getUserId());
            response.setRequestId(request.getRequestId() != null ? request.getRequestId() : UUID.randomUUID().toString());
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            log.error("Error creating response", e);
            return "{}";
        }
    }
} 