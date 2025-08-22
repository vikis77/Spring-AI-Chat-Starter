package com.randb.springaichatstarter.controller;

import com.randb.springaichatstarter.autoconfigure.SpringAiChatProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket控制器
 * 提供WebSocket相关的信息和帮助接口
 */
@RestController
@RequestMapping("/dev/api/chat/ws")
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "spring.ai.chat.websocket", name = "enabled", havingValue = "true", matchIfMissing = true)
public class WebSocketController {

    private final SpringAiChatProperties properties;

    /**
     * 获取WebSocket配置信息
     * @return WebSocket配置信息
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getWebSocketInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("enabled", properties.getWebsocket().isEnabled());
        info.put("path", properties.getWebsocket().getPath());
        info.put("url", "ws://" + getServerAddress() + properties.getWebsocket().getPath());
        info.put("usage", getUsageExample());
        
        return ResponseEntity.ok(info);
    }
    
    private String getServerAddress() {
        // 这里简化处理，实际应用中可能需要获取真实的服务器地址和端口
        return "localhost:8180";
    }
    
    private Map<String, Object> getUsageExample() {
        Map<String, Object> usage = new HashMap<>();
        
        // 客户端发送的消息示例
        Map<String, Object> requestExample = new HashMap<>();
        requestExample.put("userId", "user-123");
        requestExample.put("prompt", "你好，请介绍一下自己");
        requestExample.put("model", "qwen");
        requestExample.put("requestId", "req-" + System.currentTimeMillis());
        usage.put("request", requestExample);
        
        // 服务端返回的消息示例
        Map<String, Object> responseExample = new HashMap<>();
        responseExample.put("userId", "user-123");
        responseExample.put("requestId", "req-" + System.currentTimeMillis());
        responseExample.put("content", "你好！我是AI助手，很高兴为你服务。");
        usage.put("response", responseExample);
        
        // 使用说明
        usage.put("instructions", "建立WebSocket连接后，发送JSON格式的请求消息，服务器会返回流式的JSON响应");
        
        return usage;
    }
} 