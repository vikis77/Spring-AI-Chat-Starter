package com.randb.springaichatstarter.autoconfigure;
// package com.randb.springaichatstarter.config;

// import org.springframework.ai.chat.client.ChatClient;
// import org.springframework.ai.chat.model.ChatModel;
// import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
// import lombok.extern.slf4j.Slf4j;

// /**
//  * 此配置类已被DashScopeModelAutoConfig替代
//  * 避免Bean冲突，暂时注释掉
//  */
// // @Configuration
// @Slf4j
// public class ChatClientConfig {

//     // @Bean
//     @ConditionalOnBean(ChatModel.class)
//     public ChatClient chatClient(ChatModel chatModel) {
//         log.info("创建ChatClient Bean，使用ChatModel: {}", chatModel);
//         ChatClient client = ChatClient.builder(chatModel)
//                 .defaultSystem("你是一个友好的AI助手，能够提供有用的信息和帮助。")
//                 .build();
//         log.info("ChatClient创建成功: {}", client);
//         return client;
//     }
// } 