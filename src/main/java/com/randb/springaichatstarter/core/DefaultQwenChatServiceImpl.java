package com.randb.springaichatstarter.core;

import com.randb.springaichatstarter.dto.ChatRequest;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * 默认的通义千问实现
 * 当没有配置ChatClient时使用此实现
 * @Date: 2025-07-11 13:09:33
 * @Author: randb
 */
@Slf4j
@Service
@ConditionalOnMissingBean(name = "qwen")
public class DefaultQwenChatServiceImpl implements ChatService {

    public DefaultQwenChatServiceImpl() {
        log.warn("创建DefaultQwenChatServiceImpl - 这是一个默认实现，不会调用真正的AI服务");
    }

    @Override
    public Flux<String> streamReply(ChatRequest req) {
        log.warn("使用默认的通义千问实现(DefaultQwenChatServiceImpl)进行流式回复，这不会调用真正的AI服务");
        return Flux.just("这是默认的通义千问回复（流式）: " + req.getPrompt());
    }

    @Override
    public String syncReply(ChatRequest req) {
        log.warn("使用默认的通义千问实现(DefaultQwenChatServiceImpl)进行同步回复，这不会调用真正的AI服务");
        return "这是默认的通义千问回复（同步）: " + req.getPrompt();
    }
}