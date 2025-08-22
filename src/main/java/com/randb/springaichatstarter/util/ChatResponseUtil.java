package com.randb.springaichatstarter.util;

import com.randb.springaichatstarter.dto.ChatRequest;
import com.randb.springaichatstarter.dto.ChatResponse;

/**
 * ChatResponse工具类
 * 提供便捷的方法来创建ChatResponse对象
 * @Date: 2025-08-17
 * @Author: randb
 */
public class ChatResponseUtil {

    /**
     * 创建消息类型的ChatResponse
     * @param request 原始请求
     * @param content 响应内容
     * @return ChatResponse对象
     */
    public static ChatResponse createMessage(ChatRequest request, String content) {
        return createResponse("message", request, content);
    }

    /**
     * 创建错误类型的ChatResponse
     * @param request 原始请求
     * @param errorMessage 错误信息
     * @return ChatResponse对象
     */
    public static ChatResponse createError(ChatRequest request, String errorMessage) {
        return createResponse("error", request, errorMessage);
    }

    /**
     * 创建完成类型的ChatResponse
     * @param request 原始请求
     * @return ChatResponse对象
     */
    public static ChatResponse createCompleted(ChatRequest request) {
        return createResponse("completed", request, "[DONE]");
    }

    /**
     * 创建心跳响应的ChatResponse
     * @return ChatResponse对象
     */
    public static ChatResponse createPong() {
        ChatResponse response = new ChatResponse();
        response.setType("pong");
        response.setContent("pong");
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }

    /**
     * 创建通用的ChatResponse
     * @param type 响应类型
     * @param request 原始请求
     * @param content 响应内容
     * @return ChatResponse对象
     */
    public static ChatResponse createResponse(String type, ChatRequest request, String content) {
        ChatResponse response = new ChatResponse();
        response.setType(type);
        response.setRequestId(request != null ? request.getRequestId() : null);
        response.setUserId(request != null ? request.getUserId() : null);
        response.setContent(content);
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }

    /**
     * 创建系统响应的ChatResponse
     * @param type 响应类型
     * @param content 响应内容
     * @return ChatResponse对象
     */
    public static ChatResponse createSystemResponse(String type, String content) {
        ChatResponse response = new ChatResponse();
        response.setType(type);
        response.setContent(content);
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }
}
