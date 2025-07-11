package com.randb.springaichatstarter.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.randb.springaichatstarter.core.ChatModelFactory;
import com.randb.springaichatstarter.websocket.ChatWebSocketHandler;

/**
 * WebSocket自动配置类
 * @Date: 2025-07-11 13:09:33
 * @Author: randb
 */
@Configuration
@EnableWebSocket
@ConditionalOnClass(WebSocketConfigurer.class)
@ConditionalOnProperty(prefix = "spring.ai.chat.websocket", name = "enabled", havingValue = "true", matchIfMissing = true)
public class WebSocketConfiguration implements WebSocketConfigurer {

    private final SpringAiChatProperties properties;
    private final ChatModelFactory chatModelFactory;
    private final ObjectMapper objectMapper;

    public WebSocketConfiguration(SpringAiChatProperties properties, 
                                  ChatModelFactory chatModelFactory,
                                  ObjectMapper objectMapper) {
        this.properties = properties;
        this.chatModelFactory = chatModelFactory;
        this.objectMapper = objectMapper;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler(), properties.getWebsocket().getPath())
                .setAllowedOrigins("*");
    }

    @Bean(name = "chatWebSocketHandler")
    @ConditionalOnMissingBean(name = "chatWebSocketHandler")
    public WebSocketHandler chatWebSocketHandler() {
        return new ChatWebSocketHandler(chatModelFactory, objectMapper);
    }

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(65536);
        container.setMaxBinaryMessageBufferSize(65536);
        container.setMaxSessionIdleTimeout(60000L);
        return container;
    }
} 