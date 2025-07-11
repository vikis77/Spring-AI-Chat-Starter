// package com.randb.springaichatstarter.controller;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.context.ApplicationContext;
// import org.springframework.core.env.Environment;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;
// import lombok.extern.slf4j.Slf4j;

// import java.util.Arrays;
// import java.util.HashMap;
// import java.util.Map;
// import java.util.stream.Collectors;

// import org.springframework.ai.chat.client.ChatClient;

// /**
//  * 调试控制器
//  * 用于查看应用的状态
//  */
// @RestController
// @RequestMapping("/dev/debug")
// @Slf4j
// public class DebugController {

//     @Autowired
//     private ApplicationContext applicationContext;
    
//     @Autowired
//     private Environment environment;

//     @GetMapping("/info")
//     public Map<String, Object> getInfo() {
//         Map<String, Object> result = new HashMap<>();
        
//         // 基本信息
//         result.put("activeProfiles", Arrays.asList(environment.getActiveProfiles()));
//         result.put("javaVersion", System.getProperty("java.version"));
//         result.put("springBootVersion", SpringBootVersionHelper.getVersion());
        
//         // 配置信息
//         Map<String, Object> configs = new HashMap<>();
//         configs.put("spring.ai.chat.enabled", environment.getProperty("spring.ai.chat.enabled"));
//         configs.put("spring.ai.dashscope.api-key", 
//                 environment.getProperty("spring.ai.dashscope.api-key") != null ? "已配置" : "未配置");
//         configs.put("spring.ai.dashscope.chat.options.model", 
//                 environment.getProperty("spring.ai.dashscope.chat.options.model"));
//         result.put("configs", configs);
        
//         // Bean信息
//         Map<String, Object> beans = new HashMap<>();
//         beans.put("totalBeans", applicationContext.getBeanDefinitionNames().length);
        
//         // 检查关键Bean是否存在
//         beans.put("hasChatClient", applicationContext.containsBean("chatClient"));
//         beans.put("hasChatModel", applicationContext.containsBean("chatModel"));
//         beans.put("hasQwenChatService", applicationContext.containsBean("qwen"));
        
//         // 查找所有AI相关的Bean
//         Map<String, String> aiRelatedBeans = new HashMap<>();
//         for (String name : applicationContext.getBeanDefinitionNames()) {
//             if (name.contains("Chat") || name.contains("chat") || 
//                 name.contains("AI") || name.contains("ai") ||
//                 name.contains("Qwen") || name.contains("qwen")) {
//                 Object bean = applicationContext.getBean(name);
//                 aiRelatedBeans.put(name, bean.getClass().getName());
//             }
//         }
//         beans.put("aiRelatedBeans", aiRelatedBeans);
        
//         result.put("beans", beans);
        
//         // 记录日志
//         log.info("调试信息: {}", result);
        
//         return result;
//     }
    
//     @GetMapping("/test-qwen")
//     public String testQwenApi() {
//         try {
//             // 检查是否存在ChatClient
//             if (!applicationContext.containsBean("chatClient")) {
//                 return "错误：ChatClient Bean不存在，请检查配置";
//             }
            
//             // 获取ChatClient
//             ChatClient chatClient = applicationContext.getBean(ChatClient.class);
//             log.info("获取到ChatClient: {}", chatClient.getClass().getName());
            
//             // 测试调用
//             String response = chatClient.prompt()
//                     .user("你好，请用一句话介绍自己")
//                     .call()
//                     .content();
            
//             log.info("成功调用通义千问API，响应: {}", response);
//             return "API测试成功，响应: " + response;
//         } catch (Exception e) {
//             log.error("测试通义千问API失败", e);
//             return "API测试失败: " + e.getMessage();
//         }
//     }

//     /**
//      * 辅助类，用于获取Spring Boot版本
//      */
//     private static class SpringBootVersionHelper {
//         public static String getVersion() {
//             try {
//                 Package pkg = org.springframework.boot.SpringBootVersion.class.getPackage();
//                 return pkg.getImplementationVersion();
//             } catch (Exception e) {
//                 return "unknown";
//             }
//         }
//     }
// } 