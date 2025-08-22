package com.randb.springaichatstarter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

/**
 * 聊天响应DTO
 * @Date: 2025-07-11 13:09:33
 * @Author: randb
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatResponse implements Serializable {
    private String type;      // message | error | completed | pong
    private String requestId;   // 请求ID
    private String userId;      // 用户ID
    private String content;     // 回复内容
    private long timestamp = System.currentTimeMillis();  // 时间戳
} 
