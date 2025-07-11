package com.randb.springaichatstarter.mq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.randb.springaichatstarter.autoconfigure.SpringAiChatProperties;
import com.randb.springaichatstarter.core.ChatModelFactory;
import com.randb.springaichatstarter.core.ChatService;
import com.randb.springaichatstarter.dto.ChatRequest;
import com.randb.springaichatstarter.dto.ChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.UUID;

/**
* @Description: 
* @Date: 2025-07-11 13:09:33
* @Author: randb
*/
@Service
@Slf4j
@ConditionalOnProperty(prefix = "spring.ai.chat.rabbitmq", name = "enabled", havingValue = "true")
public class ChatMessageService {

    private final RabbitTemplate rabbitTemplate;
    private final ChatModelFactory chatModelFactory;
    private final ObjectMapper objectMapper;
    private final SpringAiChatProperties properties;
    private final ChatResponseReceiver responseReceiver;

    public ChatMessageService(RabbitTemplate rabbitTemplate, 
                             ChatModelFactory chatModelFactory, 
                             ObjectMapper objectMapper,
                             SpringAiChatProperties properties,
                             ChatResponseReceiver responseReceiver) {
        this.rabbitTemplate = rabbitTemplate;
        this.chatModelFactory = chatModelFactory;
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.responseReceiver = responseReceiver;
    }
    
    @PostConstruct
    public void init() {
        log.info("ChatMessageService initialized with exchange={}, queue={}, routingKey={}, replyQueue={}, replyRoutingKey={}", 
                properties.getRabbitmq().getExchange(),
                properties.getRabbitmq().getQueue(),
                properties.getRabbitmq().getRoutingKey(),
                properties.getRabbitmq().getReplyQueue(),
                properties.getRabbitmq().getReplyRoutingKey());
                
        log.info("RabbitMQ功能状态: {}", properties.getRabbitmq().isEnabled() ? "已启用" : "已禁用");
        if (properties.getRabbitmq().isEnabled()) {
            log.info("RabbitMQ监听器已配置，将监听队列: {}", properties.getRabbitmq().getQueue());
            log.info("回复队列已配置: {}", properties.getRabbitmq().getReplyQueue());
        }
    }

    /**
     * 发送聊天请求到队列
     * @return 请求ID，可用于跟踪请求
     */
    public String sendChatRequest(ChatRequest request) {
        try {
            if (request.getRequestId() == null) {
                request.setRequestId(UUID.randomUUID().toString());
            }
            
            // 如果没有指定回复队列，使用默认回复队列
            if (request.getReplyTo() == null) {
                request.setReplyTo(properties.getRabbitmq().getReplyQueue());
            }
            
            log.info("Sending chat request to queue: {}", request);
            rabbitTemplate.convertAndSend(
                    properties.getRabbitmq().getExchange(),
                    properties.getRabbitmq().getRoutingKey(),
                    objectMapper.writeValueAsString(request)
            );
            
            return request.getRequestId();
        } catch (JsonProcessingException e) {
            log.error("Error serializing chat request", e);
            return null;
        }
    }

    /**
     * 监听聊天请求队列
     */
    @RabbitListener(queues = "${spring.ai.chat.rabbitmq.queue:chat.queue}")
    public void processChatRequest(String message) {
        try {
            ChatRequest request = objectMapper.readValue(message, ChatRequest.class);
            log.info("Received chat request from queue: {}", request);
            
            if (request.getModel() == null || request.getModel().isEmpty()) {
                request.setModel("qwen"); // 默认使用通义千问
            }
            
            ChatService chatService = chatModelFactory.get(request.getModel());
            
            // 同步处理请求
            String reply = chatService.syncReply(request);
            
            // 创建响应
            ChatResponse response = new ChatResponse();
            response.setRequestId(request.getRequestId());
            response.setUserId(request.getUserId());
            response.setContent(reply);
            response.setTimestamp(System.currentTimeMillis());
            
            log.info("Processed chat request: {}, response length: {}", request.getRequestId(), reply.length());
            
            // 发送响应到回复队列
            String replyTo = request.getReplyTo() != null ? 
                    request.getReplyTo() : properties.getRabbitmq().getReplyQueue();
            
            rabbitTemplate.convertAndSend(
                    properties.getRabbitmq().getExchange(),
                    replyTo,
                    objectMapper.writeValueAsString(response)
            );
            log.info("Sent response to reply queue {}", replyTo);
            
        } catch (Exception e) {
            log.error("Error processing chat request from queue", e);
        }
    }
    
    /**
     * 监听回复队列
     * 上游服务可以使用此方法获取AI回复结果
     * 注意：此功能现在由ChatResponseReceiver直接处理
     */
    // @RabbitListener(queues = "${spring.ai.chat.rabbitmq.reply-queue:chat.reply}")
    // public void processReply(String message) {
    //     try {
    //         ChatResponse response = objectMapper.readValue(message, ChatResponse.class);
    //         log.info("Received chat response from reply queue: requestId={}, contentLength={}", 
    //                 response.getRequestId(), 
    //                 response.getContent() != null ? response.getContent().length() : 0);
            
    //         // 将响应传递给ChatResponseReceiver
    //         responseReceiver.receiveResponse(response);
            
    //     } catch (Exception e) {
    //         log.error("Error processing chat response from reply queue", e);
    //     }
    // }
    
    /**
     * 同步发送请求并等待响应
     * @param request 聊天请求
     * @param timeout 超时时间(毫秒)
     * @return 聊天响应，如果超时则返回null
     */
    public ChatResponse sendAndWaitForResponse(ChatRequest request, long timeout) {
        String requestId = sendChatRequest(request);
        if (requestId == null) {
            return null;
        }
        
        log.info("Waiting for response for requestId: {}, timeout: {}ms", requestId, timeout);
        return responseReceiver.waitForResponse(requestId, timeout);
    }
} 