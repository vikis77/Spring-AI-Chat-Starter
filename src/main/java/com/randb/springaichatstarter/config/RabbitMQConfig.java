package com.randb.springaichatstarter.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置类
 * 只有在spring.ai.chat.rabbitmq.enabled=true时才启用
 * @Date: 2025-07-11 13:09:33
 * @Author: randb
 */
@Configuration
@ConditionalOnProperty(prefix = "spring.ai.chat.rabbitmq", name = "enabled", havingValue = "true")
public class RabbitMQConfig {

    @Value("${spring.ai.chat.rabbitmq.queue:chat.queue}")
    private String chatQueue;
    
    @Value("${spring.ai.chat.rabbitmq.exchange:chat.exchange}")
    private String chatExchange;
    
    @Value("${spring.ai.chat.rabbitmq.routing-key:chat.request}")
    private String chatRoutingKey;
    
    @Value("${spring.ai.chat.rabbitmq.reply-queue:chat.reply}")
    private String replyQueue;
    
    @Value("${spring.ai.chat.rabbitmq.reply-routing-key:chat.reply}")
    private String replyRoutingKey;

    @Bean
    public Queue chatQueue() {
        return new Queue(chatQueue, true);
    }
    
    @Bean
    public Queue replyQueue() {
        return new Queue(replyQueue, true);
    }

    @Bean
    public DirectExchange chatExchange() {
        return new DirectExchange(chatExchange);
    }

    @Bean
    public Binding chatBinding() {
        return BindingBuilder.bind(chatQueue()).to(chatExchange()).with(chatRoutingKey);
    }
    
    @Bean
    public Binding replyBinding() {
        return BindingBuilder.bind(replyQueue()).to(chatExchange()).with(replyRoutingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
} 