package com.randb.springaichatstarter.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 应用启动监听器
 * 用于在应用启动时检查Bean的创建情况
 * @Date: 2025-07-11 13:09:33
 * @Author: randb
 */
@Component
@Slf4j
public class StartupListener implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();
        
        log.info("应用启动完成，开始检查Bean创建情况");
        
        // 检查关键Bean是否存在
        boolean hasChatClient = applicationContext.containsBean("chatClient");
        boolean hasChatModel = applicationContext.containsBean("chatModel");
        boolean hasQwenService = applicationContext.containsBean("qwen");
        
        log.info("ChatClient Bean存在: {}", hasChatClient);
        log.info("ChatModel Bean存在: {}", hasChatModel);
        log.info("QwenChatService Bean存在: {}", hasQwenService);
        
        // 查找所有相关的Bean
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        Map<String, String> aiRelatedBeans = new HashMap<>();
        
        for (String name : beanNames) {
            if (name.contains("Chat") || name.contains("chat") || 
                name.contains("AI") || name.contains("ai") ||
                name.contains("Qwen") || name.contains("qwen")) {
                try {
                    Object bean = applicationContext.getBean(name);
                    aiRelatedBeans.put(name, bean.getClass().getName());
                } catch (Exception e) {
                    log.warn("获取Bean [{}] 失败: {}", name, e.getMessage());
                    aiRelatedBeans.put(name, "获取失败: " + e.getMessage());
                }
            }
        }
        
        // 打印AI相关的Bean
        log.info("AI相关的Bean列表:");
        aiRelatedBeans.forEach((name, className) -> {
            log.info("  {} -> {}", name, className);
        });
    }
} 