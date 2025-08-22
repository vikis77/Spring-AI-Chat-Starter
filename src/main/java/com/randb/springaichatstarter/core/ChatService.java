package com.randb.springaichatstarter.core;

import com.randb.springaichatstarter.dto.ChatRequest;
import com.randb.springaichatstarter.dto.ChatResponse;
import reactor.core.publisher.Flux;

/**
 * 统一的聊天服务接口
 * 不管使用哪种模型，都通过这个接口进行交互
 * @Date: 2025-07-11 13:09:33
 * @Author: randb
 */
public interface ChatService {
    /**
     * 流式返回（适用于SSE/WebSocket）
     * @param request 聊天请求
     * @return 流式响应对象
     */
    Flux<ChatResponse> streamReply(ChatRequest request);

    /**
     * 同步返回（用于HTTP普通问答）
     * @param request 聊天请求
     * @return 完整响应对象
     */
    ChatResponse syncReply(ChatRequest request);
}
