package com.randb.springaichatstarter.autoconfigure;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.randb.springaichatstarter.mq.ChatMessageService;
import com.randb.springaichatstarter.core.ChatModelFactory;
import com.randb.springaichatstarter.mq.ChatResponseReceiver;

/**
 * RabbitMQ自动配置类
 * @Date: 2025-07-11 13:09:33
 * @Author: randb
 */
@Configuration
@ConditionalOnClass(RabbitTemplate.class)
@ConditionalOnProperty(prefix = "spring.ai.chat.rabbitmq", name = "enabled", havingValue = "true", matchIfMissing = false)
public class RabbitMQConfiguration {

    private final SpringAiChatProperties properties;

    public RabbitMQConfiguration(SpringAiChatProperties properties) {
        this.properties = properties;
    }

    @Bean
    public DirectExchange chatExchange() {
        return new DirectExchange(properties.getRabbitmq().getExchange());
    }

    @Bean
    public Queue chatQueue() {
        return new Queue(properties.getRabbitmq().getQueue());
    }
    
    @Bean
    public Queue replyQueue() {
        return new Queue(properties.getRabbitmq().getReplyQueue());
    }

    @Bean
    public Binding binding(Queue chatQueue, DirectExchange chatExchange) {
        return BindingBuilder.bind(chatQueue)
                .to(chatExchange)
                .with(properties.getRabbitmq().getRoutingKey());
    }
    
    @Bean
    public Binding replyBinding(Queue replyQueue, DirectExchange chatExchange) {
        return BindingBuilder.bind(replyQueue)
                .to(chatExchange)
                .with(properties.getRabbitmq().getReplyRoutingKey());
    }

    @Bean
    @ConditionalOnMissingBean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
    
    @Bean
    @ConditionalOnMissingBean
    public ChatResponseReceiver chatResponseReceiver(ObjectMapper objectMapper) {
        return new ChatResponseReceiver(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public ChatMessageService chatMessageService(RabbitTemplate rabbitTemplate, 
                                                ChatModelFactory chatModelFactory,
                                                ObjectMapper objectMapper,
                                                @Autowired(required = false) ChatResponseReceiver responseReceiver) {
        // 如果没有ChatResponseReceiver，创建一个新的
        if (responseReceiver == null) {
            responseReceiver = new ChatResponseReceiver(objectMapper);
        }
        return new ChatMessageService(rabbitTemplate, chatModelFactory, objectMapper, properties, responseReceiver);
    }
} 