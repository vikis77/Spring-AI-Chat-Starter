package com.randb.springaichatstarter.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 聊天响应DTO
 * @Date: 2025-07-11 13:09:33
 * @Author: randb
 */
@Data
public class ChatResponse implements Serializable {
    private String requestId;   // 请求ID
    private String userId;      // 用户ID
    private String content;     // 回复内容
    private long timestamp = System.currentTimeMillis();  // 时间戳
} 