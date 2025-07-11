package com.randb.springaichatstarter.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
* @Description: 
* @Date: 2025-07-11 13:09:33
* @Author: randb
*/
@ConfigurationProperties(prefix = "spring.ai.chat")
public class SpringAiChatProperties {
    /**
     * 是否启用AI聊天功能
     */
    private boolean enabled = true;
    
    /**
     * WebSocket配置
     */
    private WebSocketProperties websocket = new WebSocketProperties();
    
    /**
     * RabbitMQ配置
     */
    private RabbitMQProperties rabbitmq = new RabbitMQProperties();
    
    /**
     * 控制器配置
     */
    private ControllerProperties controller = new ControllerProperties();
    
    /**
     * 默认模型
     */
    private String defaultModel = "qwen";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public WebSocketProperties getWebsocket() {
        return websocket;
    }

    public void setWebsocket(WebSocketProperties websocket) {
        this.websocket = websocket;
    }

    public RabbitMQProperties getRabbitmq() {
        return rabbitmq;
    }

    public void setRabbitmq(RabbitMQProperties rabbitmq) {
        this.rabbitmq = rabbitmq;
    }
    
    public ControllerProperties getController() {
        return controller;
    }

    public void setController(ControllerProperties controller) {
        this.controller = controller;
    }

    public String getDefaultModel() {
        return defaultModel;
    }

    public void setDefaultModel(String defaultModel) {
        this.defaultModel = defaultModel;
    }

    public static class WebSocketProperties {
        /**
         * 是否启用WebSocket
         */
        private boolean enabled = true;
        
        /**
         * WebSocket路径
         */
        private String path = "/api/chat/ws";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }
    
    public static class RabbitMQProperties {
        /**
         * 是否启用RabbitMQ
         */
        private boolean enabled = false;
        
        /**
         * 交换机名称
         */
        private String exchange = "chat.exchange";
        
        /**
         * 队列名称
         */
        private String queue = "chat.queue";
        
        /**
         * 路由键
         */
        private String routingKey = "chat.request";
        
        /**
         * 回复队列名称
         */
        private String replyQueue = "chat.reply";
        
        /**
         * 回复队列路由键
         */
        private String replyRoutingKey = "chat.reply";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getExchange() {
            return exchange;
        }

        public void setExchange(String exchange) {
            this.exchange = exchange;
        }

        public String getQueue() {
            return queue;
        }

        public void setQueue(String queue) {
            this.queue = queue;
        }

        public String getRoutingKey() {
            return routingKey;
        }

        public void setRoutingKey(String routingKey) {
            this.routingKey = routingKey;
        }
        
        public String getReplyQueue() {
            return replyQueue;
        }
        
        public void setReplyQueue(String replyQueue) {
            this.replyQueue = replyQueue;
        }
        
        public String getReplyRoutingKey() {
            return replyRoutingKey;
        }
        
        public void setReplyRoutingKey(String replyRoutingKey) {
            this.replyRoutingKey = replyRoutingKey;
        }
    }
    
    public static class ControllerProperties {
        /**
         * 是否启用REST API控制器
         */
        private boolean enabled = true;
        
        /**
         * API基础路径
         */
        private String basePath = "/api/chat";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getBasePath() {
            return basePath;
        }

        public void setBasePath(String basePath) {
            this.basePath = basePath;
        }
    }
}