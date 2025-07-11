package com.randb.springaichatstarter.config;

import com.randb.springaichatstarter.core.ChatService;
import com.randb.springaichatstarter.core.DefaultQwenChatServiceImpl;
import com.randb.springaichatstarter.core.QwenChatServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import lombok.extern.slf4j.Slf4j;

/**
 * 服务配置类
 * 负责设置默认的聊天服务
 * @Date: 2025-07-11 13:09:33
 * @Author: randb
 */
@Configuration
@Slf4j
public class ServiceConfig {

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * 设置默认的聊天服务为通义千问
     */
    @Bean("defaultChatService")
    @Order(300) // 确保优先级低于QwenChatServiceImpl和DefaultQwenChatServiceImpl
    public ChatService defaultChatService() {
        // 先尝试获取QwenChatServiceImpl
        try {
            if (applicationContext.getBeanNamesForType(QwenChatServiceImpl.class).length > 0) {
                ChatService qwenService = applicationContext.getBean(QwenChatServiceImpl.class);
                log.info("成功获取QwenChatServiceImpl，将其设置为默认服务: {}", qwenService.getClass().getName());
                return qwenService;
            }
        } catch (Exception e) {
            log.warn("尝试获取QwenChatServiceImpl失败", e);
        }
        
        // 如果没有QwenChatServiceImpl，尝试获取名为qwen的Bean
        try {
            if (applicationContext.containsBean("qwen")) {
                ChatService qwenService = (ChatService) applicationContext.getBean("qwen");
                log.info("使用已注册的qwen服务: {}", qwenService.getClass().getName());
                
                // 检查是否是DefaultQwenChatServiceImpl
                if (qwenService instanceof DefaultQwenChatServiceImpl) {
                    log.warn("qwen服务是DefaultQwenChatServiceImpl，这是一个默认实现，不会调用真正的AI服务");
                } else if (qwenService instanceof QwenChatServiceImpl) {
                    log.info("qwen服务是QwenChatServiceImpl，将使用真实的AI服务");
                }
                
                return qwenService;
            }
        } catch (Exception e) {
            log.warn("尝试获取名为'qwen'的Bean失败", e);
        }
        
        // 如果所有尝试都失败，创建一个默认实现
        log.warn("没有找到任何可用的ChatService，创建默认实现DefaultQwenChatServiceImpl");
        return new DefaultQwenChatServiceImpl();
    }
} 