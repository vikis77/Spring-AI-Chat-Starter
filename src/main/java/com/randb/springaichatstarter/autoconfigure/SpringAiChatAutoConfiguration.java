package com.randb.springaichatstarter.autoconfigure;

import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.randb.springaichatstarter.config.ServiceConfig;
import com.randb.springaichatstarter.config.WebConfig;
import com.randb.springaichatstarter.core.ChatModelFactory;
import com.randb.springaichatstarter.core.ChatService;

/**
* @Description:
* @Date: 2025-07-11 13:09:33
* @Author: randb
*/

@Configuration
@EnableConfigurationProperties(SpringAiChatProperties.class)
@ConditionalOnProperty(prefix = "spring.ai.chat", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SpringAiChatAutoConfiguration {

    // @Configuration
    // @ConditionalOnWebApplication
    // @Import({WebConfig.class, ServiceConfig.class, WebControllerConfiguration.class})
    // static class WebConfiguration {
    //     // Web相关配置
    // }
    
    @Configuration
    @ConditionalOnProperty(prefix = "spring.ai.chat", name = "websocket.enabled", havingValue = "true", matchIfMissing = true)
    @Import(WebSocketConfiguration.class)
    static class WebSocketAutoConfiguration {
        // WebSocket相关配置
    }
    
    @Configuration
    @ConditionalOnProperty(prefix = "spring.ai.chat", name = "rabbitmq.enabled", havingValue = "true", matchIfMissing = false)
    @Import(RabbitMQConfiguration.class)
    static class RabbitMQAutoConfiguration {
        // RabbitMQ相关配置
    }
    
    @Bean
    @ConditionalOnMissingBean
    public ChatModelFactory chatModelFactory(Map<String, ChatService> modelMap, ApplicationContext applicationContext) {
        return new ChatModelFactory(modelMap, applicationContext);
    }
}