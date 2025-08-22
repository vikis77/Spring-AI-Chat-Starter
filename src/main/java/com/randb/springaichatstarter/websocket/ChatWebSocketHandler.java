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

    /** sessionId -> WebSocketSession */
    private final ConcurrentHashMap<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    /** userId -> sessionId */
    private final ConcurrentHashMap<String, String> userSessionMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("ChatWebSocketHandler 已创建，准备处理 WebSocket 连接");
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessionMap.put(session.getId(), session);
        log.info("WebSocket 连接建立: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        try {
            // 处理心跳
            if ("ping".equalsIgnoreCase(payload)) {
                sendMessage(session, createSystemResponse("pong", "pong"));
                return;
            }

            ChatRequest request = objectMapper.readValue(payload, ChatRequest.class);

            if (request.getModel() == null || request.getModel().isEmpty()) {
                request.setModel("qwen"); // 默认模型
            }

            if (request.getUserId() != null) {
                userSessionMap.put(request.getUserId(), session.getId());
            }

            log.info("收到 WebSocket 消息: {}", payload);

            ChatService chatService = chatModelFactory.get(request.getModel());

            chatService.streamReply(request).subscribe(
                response -> sendMessage(session, convertToJson(response)),
                error -> handleError(session, error, request),
                () -> sendMessage(session, createResponse("completed", request, "[DONE]"))
            );

        } catch (Exception e) {
            log.error("处理 WebSocket 消息异常", e);
            sendErrorMessage(session, "Error processing request: " + e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessionMap.remove(session.getId());
        // 同步清理 userId -> sessionId
        userSessionMap.entrySet().removeIf(entry -> entry.getValue().equals(session.getId()));
        log.info("WebSocket 连接关闭: {}", session.getId());
    }

    private void sendMessage(WebSocketSession session, String message) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(message));
            }
        } catch (IOException e) {
            log.error("发送 WebSocket 消息异常", e);
        }
    }

    private void handleError(WebSocketSession session, Throwable error, ChatRequest request) {
        log.error("WebSocket 流处理错误", error);
        String errorMessage = "Error in AI response: " +
                (error.getMessage() != null ? error.getMessage() : "Connection reset");
        sendMessage(session, createResponse("error", request, errorMessage));
    }

    private void sendErrorMessage(WebSocketSession session, String errorMessage) {
        try {
            String json = createSystemResponse("error", errorMessage);
            sendMessage(session, json);
        } catch (Exception e) {
            log.error("发送错误消息失败", e);
        }
    }

    private String createResponse(String type, ChatRequest request, String content) {
        try {
            ChatResponse response = new ChatResponse();
            response.setType(type);
            response.setContent(content);
            response.setUserId(request.getUserId());
            response.setRequestId(request.getRequestId() != null ? request.getRequestId() : UUID.randomUUID().toString());
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            log.error("构建响应失败", e);
            return "{}";
        }
    }

    private String createSystemResponse(String type, String content) {
        try {
            ChatResponse response = new ChatResponse();
            response.setType(type);
            response.setContent(content);
            response.setRequestId(UUID.randomUUID().toString());
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            log.error("构建系统响应失败", e);
            return "{}";
        }
    }

    private String convertToJson(ChatResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            log.error("转换ChatResponse为JSON失败", e);
            return "{}";
        }
    }
}
