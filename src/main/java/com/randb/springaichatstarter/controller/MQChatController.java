// package com.randb.springaichatstarter.controller;

// import com.randb.springaichatstarter.dto.ChatRequest;
// import com.randb.springaichatstarter.dto.ChatResponse;
// import com.randb.springaichatstarter.mq.ChatMessageService;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.util.UUID;

// /**
//  * 消息队列聊天控制器
//  * 提供基于RabbitMQ的聊天API
//  */
// @RestController
// @RequestMapping("/dev/api/chat/mq")
// @Slf4j
// @ConditionalOnProperty(prefix = "spring.ai.chat.rabbitmq", name = "enabled", havingValue = "true")
// public class MQChatController {

//     private final ChatMessageService chatMessageService;

//     public MQChatController(ChatMessageService chatMessageService) {
//         this.chatMessageService = chatMessageService;
//     }

//     /**
//      * 异步聊天API - 发送请求到队列，不等待响应
//      */
//     @PostMapping("/async")
//     public ResponseEntity<String> asyncChat(@RequestBody ChatRequest request) {
//         log.info("Received chat request: {}", request);
        
//         if (request.getRequestId() == null) {
//             request.setRequestId(UUID.randomUUID().toString());
//         }
        
//         String requestId = chatMessageService.sendChatRequest(request);
//         if (requestId != null) {
//             return ResponseEntity.ok(requestId);
//         } else {
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send request");
//         }
//     }
    
//     /**
//      * 同步聊天API - 发送请求到队列，等待响应
//      * 最多等待10秒
//      */
//     @PostMapping("/sync")
//     public ResponseEntity<ChatResponse> syncChat(@RequestBody ChatRequest request) {
//         log.info("Received chat request: {}", request);
        
//         if (request.getRequestId() == null) {
//             request.setRequestId(UUID.randomUUID().toString());
//         }
        
//         ChatResponse response = chatMessageService.sendAndWaitForResponse(request, 10000);
//         if (response != null) {
//             return ResponseEntity.ok(response);
//         } else {
//             return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(null);
//         }
//     }
// } 