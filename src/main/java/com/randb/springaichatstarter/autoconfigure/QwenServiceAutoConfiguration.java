package com.randb.springaichatstarter.autoconfigure;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.randb.springaichatstarter.core.ChatService;
import com.randb.springaichatstarter.core.QwenChatServiceImpl;

import lombok.extern.slf4j.Slf4j;

/**
 * 通义千问服务自动配置
 * 当存在ChatClient时，自动创建QwenChatServiceImpl
 * @Date: 2025-07-11 13:09:33
 * @Author: randb
 */
@Configuration
@AutoConfiguration
@AutoConfigureAfter(DashScopeModelAutoConfig.class)
@AutoConfigureBefore(ChatServiceAutoConfiguration.class)
@ConditionalOnProperty(prefix = "spring.ai.chat", name = "enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class QwenServiceAutoConfiguration {

    /**
     * 当存在ChatClient时，创建QwenChatServiceImpl
     */
    @Bean("qwen")
    @Primary
    @ConditionalOnBean(ChatClient.class)
    @ConditionalOnMissingBean(name = "qwen")
    public ChatService qwenChatService(ApplicationContext applicationContext) {
        log.info("创建QwenChatServiceImpl - 这将使用真实的AI服务");
        return new QwenChatServiceImpl(applicationContext);
    }
} 