package com.randb.springaichatstarter.core;

import com.randb.springaichatstarter.dto.ChatRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import reactor.core.publisher.Flux;

import jakarta.annotation.PostConstruct;

/**
 * 通义千问实现类
 * 依赖于ChatClient
 * @Date: 2025-07-11 13:09:33
 * @Author: randb
 */
@Order(100)  // 确保这个Bean有更高的优先级（数值越小优先级越高）
@Slf4j
public class QwenChatServiceImpl implements ChatService {

    private ChatClient chatClient;
    private final DefaultQwenChatServiceImpl fallbackService;
    private final ApplicationContext applicationContext;

    @PostConstruct
    public void init() {
        log.info("QwenChatServiceImpl(@Order(100)) Bean已创建，将被用于处理AI请求");
        
        // 尝试从ApplicationContext获取ChatClient，解决Bean初始化顺序问题
        try {
            if (this.chatClient == null && this.applicationContext != null) {
                String[] beanNames = this.applicationContext.getBeanNamesForType(ChatClient.class);
                if (beanNames != null && beanNames.length > 0) {
                    this.chatClient = this.applicationContext.getBean(ChatClient.class);
                    log.info("在PostConstruct中成功获取ChatClient: {}", chatClient.getClass().getName());
                }
            }
        } catch (Exception e) {
            log.error("尝试获取ChatClient失败", e);
        }
        
        log.info("使用的ChatClient实现类: {}", chatClient != null ? chatClient.getClass().getName() : "ChatClient为null，将使用降级服务");
    }

    public QwenChatServiceImpl(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.fallbackService = new DefaultQwenChatServiceImpl();
        
        // 尝试立即从ApplicationContext获取ChatClient
        try {
            String[] beanNames = applicationContext.getBeanNamesForType(ChatClient.class);
            if (beanNames != null && beanNames.length > 0) {
                this.chatClient = applicationContext.getBean(ChatClient.class);
                log.info("在构造函数中成功获取ChatClient: {}", chatClient.getClass().getName());
            } else {
                log.warn("未找到ChatClient Bean，将在init方法中重试");
            }
        } catch (Exception e) {
            log.error("尝试获取ChatClient失败，将在init方法中重试", e);
        }
    }

    /**
     * 获取ChatClient，如果不可用则返回null
     */
    private ChatClient getChatClient() {
        // 如果已经有ChatClient，直接返回
        if (chatClient != null) {
            return chatClient;
        }
        
        // 尝试从ApplicationContext获取
        try {
            String[] beanNames = applicationContext.getBeanNamesForType(ChatClient.class);
            if (beanNames != null && beanNames.length > 0) {
                chatClient = applicationContext.getBean(ChatClient.class);
                log.info("成功获取ChatClient: {}", chatClient.getClass().getName());
                return chatClient;
            }
        } catch (Exception e) {
            log.warn("获取ChatClient失败", e);
        }
        
        return null;
    }

    @Override
    public Flux<String> streamReply(ChatRequest req) {
        log.info("Qwen streaming reply for prompt: {}", req.getPrompt());
        
        // 获取ChatClient
        ChatClient client = getChatClient();
        if (client == null) {
            log.warn("ChatClient不可用，使用降级服务进行流式回复");
            return fallbackService.streamReply(req);
        }
        
        try {
            log.debug("准备调用ChatClient.prompt().user().stream()");
            Flux<String> responseFlux = client.prompt()
                    .user(req.getPrompt())
                    .stream()
                    .content();
            
            // 在Flux上应用操作符
            return responseFlux
                    .doOnSubscribe(s -> log.debug("Stream已订阅"))
                    .doOnNext(chunk -> log.debug("收到流式响应片段: {}", chunk.length() > 20 ? chunk.substring(0, 20) + "..." : chunk))
                    .doOnComplete(() -> log.debug("流式响应完成"))
                    .doOnError(e -> {
                        log.error("流式响应出错: {}", e.getMessage());
                        // 不在这里处理错误，让它传播到下面的catch块
                    });
        } catch (Exception e) {
            log.error("处理流式回复时发生异常: {}，将使用降级服务", e.getMessage(), e);
            return fallbackService.streamReply(req)
                    .doOnSubscribe(s -> log.warn("使用降级服务进行流式回复"));
        }
    }

    @Override
    public String syncReply(ChatRequest req) {
        log.info("Qwen sync reply for prompt: {}", req.getPrompt());
        
        // 获取ChatClient
        ChatClient client = getChatClient();
        if (client == null) {
            log.warn("ChatClient不可用，使用降级服务进行同步回复");
            return fallbackService.syncReply(req);
        }
        
        try {
            log.debug("准备调用ChatClient.prompt().user().call()");
            String content = client.prompt()
                    .user(req.getPrompt())
                    .call()
                    .content();
            
            log.debug("收到同步响应: {}", content.length() > 50 ? content.substring(0, 50) + "..." : content);
            return content;
        } catch (Exception e) {
            log.error("处理同步回复时发生异常: {}，将使用降级服务", e.getMessage(), e);
            return fallbackService.syncReply(req) + "\n\n[注: 由于API调用错误，此为降级服务回复]";
        }
    }
}
