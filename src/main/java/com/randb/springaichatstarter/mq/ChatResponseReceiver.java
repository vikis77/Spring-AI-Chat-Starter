package com.randb.springaichatstarter.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.randb.springaichatstarter.dto.ChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 聊天响应接收器
 * 用于接收和处理回复消息，提供同步等待响应的功能
 * @Date: 2025-07-11 13:09:33
 * @Author: randb
 */
@Component
@Slf4j
@ConditionalOnProperty(prefix = "spring.ai.chat.rabbitmq", name = "enabled", havingValue = "true")
public class ChatResponseReceiver {
    
    private final Map<String, ChatResponse> responseCache = new ConcurrentHashMap<>();
    private final Map<String, CountDownLatch> latches = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    
    public ChatResponseReceiver(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        log.info("ChatResponseReceiver初始化成功");
    }
    
    /**
     * 监听回复队列
     * 接收AI回复结果
     */
    // @RabbitListener(queues = "${spring.ai.chat.rabbitmq.reply-queue:chat.reply}")
    // public void processReply(String message) {
    //     try {
    //         ChatResponse response = objectMapper.readValue(message, ChatResponse.class);
    //         log.info("接收到MQ回复: requestId={}, contentLength={}", 
    //                 response.getRequestId(), 
    //                 response.getContent() != null ? response.getContent().length() : 0);
            
    //         // 缓存响应
    //         receiveResponse(response);
            
    //     } catch (Exception e) {
    //         log.error("处理MQ回复时出错", e);
    //     }
    // }
    
    /**
     * 接收来自回复队列的消息
     * 此方法由ChatMessageService.processReply调用
     */
    public void receiveResponse(ChatResponse response) {
        String requestId = response.getRequestId();
        
        // 缓存响应
        responseCache.put(requestId, response);
        log.debug("Response cached for requestId: {}", requestId);
        
        // 如果有等待此响应的线程，通知它
        CountDownLatch latch = latches.get(requestId);
        if (latch != null) {
            latch.countDown();
            log.debug("Notified waiting thread for requestId: {}", requestId);
        }
    }
    
    /**
     * 同步等待响应
     * @param requestId 请求ID
     * @param timeout 超时时间(毫秒)
     * @return 响应对象，如果超时则返回null
     */
    public ChatResponse waitForResponse(String requestId, long timeout) {
        // 如果已经有响应，直接返回
        if (responseCache.containsKey(requestId)) {
            log.debug("Response already in cache for requestId: {}", requestId);
            return responseCache.remove(requestId);
        }
        
        // 创建等待锁
        CountDownLatch latch = new CountDownLatch(1);
        latches.put(requestId, latch);
        log.debug("Waiting for response with requestId: {}, timeout: {}ms", requestId, timeout);
        
        try {
            // 等待响应或超时
            boolean received = latch.await(timeout, TimeUnit.MILLISECONDS);
            if (received) {
                log.debug("Response received for requestId: {}", requestId);
                return responseCache.remove(requestId);
            } else {
                log.warn("Timeout waiting for response: {}", requestId);
                return null;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while waiting for response", e);
            return null;
        } finally {
            latches.remove(requestId);
        }
    }
    
    /**
     * 清理过期的响应
     * 可以定期调用此方法清理缓存
     */
    public void cleanupExpiredResponses(long maxAgeMillis) {
        long now = System.currentTimeMillis();
        int removedCount = 0;
        
        for (Map.Entry<String, ChatResponse> entry : responseCache.entrySet()) {
            ChatResponse response = entry.getValue();
            if ((now - response.getTimestamp()) > maxAgeMillis) {
                responseCache.remove(entry.getKey());
                removedCount++;
            }
        }
        
        if (removedCount > 0) {
            log.info("Cleaned up {} expired responses", removedCount);
        }
    }
} 