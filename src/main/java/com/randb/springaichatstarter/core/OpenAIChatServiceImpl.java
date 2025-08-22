package com.randb.springaichatstarter.core;

import com.randb.springaichatstarter.dto.ChatRequest;
import com.randb.springaichatstarter.dto.ChatResponse;
import com.randb.springaichatstarter.util.ChatResponseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import jakarta.annotation.PostConstruct;

/**
 * OpenAI实现类
 * 依赖于ChatClient
 * @Date: 2025-07-11 13:09:33
 * @Author: randb
 */
@Service("openai")
@Slf4j
@RequiredArgsConstructor
@ConditionalOnBean(ChatClient.class)
@ConditionalOnProperty(prefix = "spring.ai.chat", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OpenAIChatServiceImpl implements ChatService {

    private final ChatClient chatClient;
    
    @PostConstruct
    public void init() {
        log.info("OpenAIChatServiceImpl Bean已创建，将被用于处理AI请求");
        log.info("使用的ChatClient实现类: {}", chatClient.getClass().getName());
    }

    @Override
    public Flux<ChatResponse> streamReply(ChatRequest req) {
        log.info("OpenAI streaming reply for prompt: {}", req.getPrompt());
        try {
            log.debug("准备调用ChatClient.prompt().user().stream()");
            return chatClient.prompt()
                    .user(req.getPrompt())
                    .stream()
                    .content()
                    .map(content -> ChatResponseUtil.createMessage(req, content));
        } catch (Exception e) {
            log.error("处理流式回复时发生异常", e);
            return Flux.just(ChatResponseUtil.createError(req, "处理请求时发生错误: " + e.getMessage()));
        }
    }

    @Override
    public ChatResponse syncReply(ChatRequest req) {
        log.info("OpenAI sync reply for prompt: {}", req.getPrompt());
        try {
            log.debug("准备调用ChatClient.prompt().user().call()");
            String content = chatClient.prompt()
                    .user(req.getPrompt())
                    .call()
                    .content();

            log.debug("收到同步响应: {}", content.length() > 50 ? content.substring(0, 50) + "..." : content);

            return ChatResponseUtil.createMessage(req, content);
        } catch (Exception e) {
            log.error("处理同步回复时发生异常", e);

            return ChatResponseUtil.createError(req, "处理请求时发生错误: " + e.getMessage());
        }
    }
}
