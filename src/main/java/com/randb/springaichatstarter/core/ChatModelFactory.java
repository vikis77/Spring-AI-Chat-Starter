package com.randb.springaichatstarter.core;

import com.randb.springaichatstarter.dto.ChatRequest;
import com.randb.springaichatstarter.dto.ChatResponse;
import com.randb.springaichatstarter.util.ChatResponseUtil;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Map;

/**
 * 聊天模型工厂
 * 根据模型名称动态选择对应的聊天服务实现
 * @Date: 2025-07-11 13:09:33
 * @Author: randb
 */
@Component
@ConditionalOnMissingBean(ChatModelFactory.class)
@Slf4j
public class ChatModelFactory {

    private final Map<String, ChatService> modelMap;
    private final ChatService defaultService;
    private final ApplicationContext applicationContext;

    public ChatModelFactory(Map<String, ChatService> modelMap, ApplicationContext applicationContext) {
        this.modelMap = modelMap != null ? modelMap : Collections.emptyMap();
        this.applicationContext = applicationContext;
        
        // 尝试获取QwenChatServiceImpl作为默认服务
        ChatService qwenService = null;
        try {
            if (applicationContext.getBeanNamesForType(QwenChatServiceImpl.class).length > 0) {
                qwenService = applicationContext.getBean(QwenChatServiceImpl.class);
                log.info("找到QwenChatServiceImpl，使用它作为默认服务: {}", qwenService.getClass().getName());
            }
        } catch (Exception e) {
            log.warn("尝试获取QwenChatServiceImpl失败", e);
        }
        
        // 如果没有找到QwenChatServiceImpl，尝试使用名为"qwen"的服务
        if (qwenService == null && this.modelMap.containsKey("qwen")) {
            qwenService = this.modelMap.get("qwen");
            log.info("找到名为'qwen'的服务: {}", qwenService.getClass().getName());
        }
        
        // 如果上述方法都失败，尝试创建一个基于ChatClient的服务
        if (qwenService == null) {
            try {
                if (applicationContext.getBeanNamesForType(ChatClient.class).length > 0) {
                    ChatClient chatClient = applicationContext.getBean(ChatClient.class);
                    qwenService = new ChatClientAdapter(chatClient);
                    log.info("找到ChatClient，创建基于ChatClient的服务适配器");
                }
            } catch (Exception e) {
                log.warn("尝试获取ChatClient失败", e);
            }
        }
        
        // 如果所有方法都失败，创建一个默认实现
        this.defaultService = qwenService != null ? qwenService : new DefaultQwenChatServiceImpl();
        
        log.info("ChatModelFactory初始化完成，可用的模型: {}", modelMap != null ? modelMap.keySet() : "none");
        log.info("默认服务: {} ({})", defaultService.getClass().getName(), 
                defaultService instanceof DefaultQwenChatServiceImpl ? "默认实现，不会调用真正的AI" : "真实AI实现");
        
        if (modelMap != null && !modelMap.isEmpty()) {
            modelMap.forEach((name, service) -> {
                log.info("注册的服务: {} -> {} ({})", name, service.getClass().getName(),
                        service instanceof DefaultQwenChatServiceImpl ? "默认实现" : "真实实现");
            });
        }
    }

    /**
     * 根据模型名称获取对应的聊天服务
     * @param model 模型名称
     * @return 聊天服务实现
     */
    public ChatService get(String model) {
        if (model == null || model.isEmpty()) {
            log.info("模型名称为空，使用默认服务: {}", defaultService.getClass().getName());
            return defaultService;
        }
        
        // 尝试从ApplicationContext中获取QwenChatServiceImpl（最后的尝试）
        if ("qwen".equalsIgnoreCase(model)) {
            try {
                if (applicationContext.getBeanNamesForType(QwenChatServiceImpl.class).length > 0) {
                    ChatService qwenService = applicationContext.getBean(QwenChatServiceImpl.class);
                    if (qwenService != null) {
                        log.info("通过ApplicationContext获取到QwenChatServiceImpl: {}", qwenService.getClass().getName());
                        return qwenService;
                    }
                }
            } catch (Exception e) {
                log.warn("尝试获取QwenChatServiceImpl失败", e);
            }
        }
        
        ChatService service = modelMap.getOrDefault(model, defaultService);
        log.info("获取模型 {} 的服务: {} ({})", model, service.getClass().getName(),
                service instanceof DefaultQwenChatServiceImpl ? "默认实现，不会调用真正的AI" : "真实实现");
        return service;
    }
    
    /**
     * ChatClient适配器，将ChatClient包装为ChatService
     */
    private static class ChatClientAdapter implements ChatService {
        private final ChatClient chatClient;

        public ChatClientAdapter(ChatClient chatClient) {
            this.chatClient = chatClient;
        }

        @Override
        public Flux<ChatResponse> streamReply(ChatRequest request) {
            return chatClient.prompt()
                    .user(request.getPrompt())
                    .stream()
                    .content()
                    .map(content -> ChatResponseUtil.createMessage(request, content));
        }

        @Override
        public ChatResponse syncReply(ChatRequest request) {
            String content = chatClient.prompt()
                    .user(request.getPrompt())
                    .call()
                    .content();

            return ChatResponseUtil.createMessage(request, content);
        }
    }
    
    // 内部默认实现，确保即使没有任何ChatService也能工作
    private static class DefaultChatServiceImpl implements ChatService {
        @Override
        public Flux<ChatResponse> streamReply(ChatRequest request) {
            return Flux.just(ChatResponseUtil.createMessage(request, "默认回复（流式）: " + request.getPrompt()));
        }

        @Override
        public ChatResponse syncReply(ChatRequest request) {
            return ChatResponseUtil.createMessage(request, "默认回复（同步）: " + request.getPrompt());
        }
    }
}