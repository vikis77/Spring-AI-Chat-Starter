package com.randb.springaichatstarter.autoconfigure;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import lombok.extern.slf4j.Slf4j;

/**
 * DashScope模型自动配置
 * 主要配置ObjectMapper以处理JSON响应
 * @Date: 2025-07-11 13:09:33
 * @Author: randb
 */
@Configuration
@AutoConfigureBefore(QwenServiceAutoConfiguration.class)
@Slf4j
public class DashScopeModelAutoConfig {

    /**
     * TokenUsage的MixIn接口，用于忽略未知属性
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private interface TokenUsageMixIn {
        // 空接口，只用于标记
    }

    /**
     * 配置Jackson ObjectMapper，忽略未知属性
     * 解决通义千问API返回的JSON格式问题
     */
    @Bean("dashscopeObjectMapper")
    // @Primary
    public ObjectMapper dashscopeObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        // 配置反序列化特性
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        objectMapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
        
        // 配置序列化特性
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        
        // 添加MixIn来处理TokenUsage
        try {
            Class<?> tokenUsageClass = Class.forName("com.alibaba.cloud.ai.dashscope.api.DashScopeApi$TokenUsage");
            objectMapper.addMixIn(tokenUsageClass, TokenUsageMixIn.class);
            log.info("成功为TokenUsage添加MixIn，以忽略未知属性");
        } catch (ClassNotFoundException e) {
            log.warn("未找到TokenUsage类，无法添加MixIn: {}", e.getMessage());
        }
        
        log.info("创建增强的ObjectMapper，配置了多项特性以处理通义千问API响应");
        return objectMapper;
    }

    /**
     * 配置HTTP消息转换器，使用自定义的ObjectMapper
     */
    @Bean("dashscopeMappingJackson2HttpMessageConverter")
    @Primary
    public MappingJackson2HttpMessageConverter dashscopeMappingJackson2HttpMessageConverter(
            @Qualifier("dashscopeObjectMapper") ObjectMapper dashscopeObjectMapper) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(dashscopeObjectMapper);
        log.info("创建使用自定义ObjectMapper的MappingJackson2HttpMessageConverter");
        return converter;
    }

    /**
     * 将DashScopeChatModel注册为chatModel
     * 这是关键步骤：让Spring AI找到名为"chatModel"的Bean
     */
    @Bean
    @Primary
    @ConditionalOnBean(DashScopeChatModel.class)
    public ChatModel chatModel(DashScopeChatModel dashscopeChatModel) {
        log.info("成功创建ChatModel，使用DashScopeChatModel: {}", dashscopeChatModel);
        return dashscopeChatModel;
    }

    
    /**
     * 基于ChatModel创建ChatClient
     */
    @Bean
    @Primary
    @ConditionalOnBean(ChatModel.class)
    public ChatClient chatClient(ChatModel chatModel) {
        log.info("成功创建ChatClient，使用ChatModel: {}", chatModel);
        //初始化基于内存的对话记忆
        // ChatMemory chatMemory = MessageWindowChatMemory.builder().build();
        return ChatClient.builder(chatModel)
                // .defaultAdvisors(
                //         MessageChatMemoryAdvisor.builder(chatMemory).build()
                // )
                .defaultSystem("你是一个友好的AI助手，能够提供有用的信息和帮助。")
                .build();
    }
}