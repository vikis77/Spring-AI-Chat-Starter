// package com.randb.springaichatstarter.autoconfigure;

// import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
// import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
// import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
// import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.web.bind.annotation.RestController;

// import com.randb.springaichatstarter.controller.ChatController;
// import com.randb.springaichatstarter.controller.MQChatController;
// import com.randb.springaichatstarter.core.ChatModelFactory;
// import com.randb.springaichatstarter.mq.ChatMessageService;

// /**
//  * Web控制器自动配置类
//  */
// @Configuration
// @ConditionalOnWebApplication
// @ConditionalOnClass(RestController.class)
// public class WebControllerConfiguration {

//     /**
//      * Chat API控制器自动配置
//      */
//     @Configuration
//     @ConditionalOnProperty(prefix = "spring.ai.chat.controller", name = "enabled", havingValue = "true", matchIfMissing = true)
//     static class ChatControllerConfiguration {
        
//         @Bean
//         @ConditionalOnMissingBean
//         public ChatController chatController(ChatModelFactory chatModelFactory) {
//             return new ChatController(chatModelFactory);
//         }
//     }
    
//     /**
//      * MQ控制器自动配置
//      */
//     @Configuration
//     @ConditionalOnProperty(prefix = "spring.ai.chat.rabbitmq", name = "enabled", havingValue = "true", matchIfMissing = false)
//     static class MQChatControllerConfiguration {
        
//         @Bean
//         @ConditionalOnMissingBean
//         public MQChatController mqChatController(ChatMessageService chatMessageService) {
//             return new MQChatController(chatMessageService);
//         }
//     }
// } 