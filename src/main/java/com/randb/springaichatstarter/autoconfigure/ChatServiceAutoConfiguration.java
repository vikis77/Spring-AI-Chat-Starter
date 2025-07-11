package com.randb.springaichatstarter.autoconfigure;

import com.randb.springaichatstarter.core.ChatService;
import com.randb.springaichatstarter.core.DefaultQwenChatServiceImpl;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import lombok.extern.slf4j.Slf4j;

/**
 * 聊天服务自动配置类
 * 负责注册默认的聊天服务实现
 * @Date: 2025-07-11 13:09:33
 * @Author: randb
 */

@Configuration
@Slf4j
@AutoConfigureAfter({QwenServiceAutoConfiguration.class})
public class ChatServiceAutoConfiguration {

    /**
     * 提供默认的通义千问实现
     * 只有当没有名为"qwen"的Bean且没有ChatClient时才创建
     */
    @Bean("qwen")
    @ConditionalOnMissingBean(name = "qwen")
    @Conditional(NoChatClientCondition.class)
    @Order(200) // 确保这个Bean有更低的优先级（数值越大优先级越低）
    public ChatService defaultQwenChatService() {
        log.info("创建默认的通义千问服务实现 DefaultQwenChatServiceImpl（优先级低）");
        DefaultQwenChatServiceImpl service = new DefaultQwenChatServiceImpl();
        return service;
    }
    
    /**
     * 检查是否没有ChatClient的条件
     */
    public static class NoChatClientCondition implements org.springframework.context.annotation.Condition {
        @Override
        public boolean matches(org.springframework.context.annotation.ConditionContext context, 
                              org.springframework.core.type.AnnotatedTypeMetadata metadata) {
            return context.getBeanFactory().getBeanNamesForType(ChatClient.class).length == 0;
        }
    }
} 