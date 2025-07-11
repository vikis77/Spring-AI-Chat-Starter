package com.randb.springaichatstarter.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 聊天请求DTO
 * @Date: 2025-07-11 13:09:33
 * @Author: randb
 */
@Data
public class ChatRequest implements Serializable {
    /**
     * 请求ID，用于追踪请求
     */
    private String requestId;
    /**
     * 用户ID
     */
    private String userId;
    /**
     * 提示词
     */
    private String prompt;
    /**
     * 模型名称: qwen、openai
     */
    private String model;
    /**
     * 会话ID，用于支持上下文
     */
    private String sessionId;
    /**
     * 指定回复队列名
     */
    private String replyTo; 
    /**
     * 是否希望是流式响应
     */
    private boolean stream; 

}
