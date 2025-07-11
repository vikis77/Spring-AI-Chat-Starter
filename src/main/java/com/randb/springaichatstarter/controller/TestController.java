// package com.randb.springaichatstarter.controller;

// import com.randb.springaichatstarter.dto.ChatRequest;
// import com.randb.springaichatstarter.dto.ChatResponse;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.ai.chat.client.ChatClient;
// import org.springframework.http.MediaType;
// import org.springframework.web.bind.annotation.*;
// import reactor.core.publisher.Flux;

// import java.util.UUID;

// @RestController
// @RequestMapping("/dev/api/test")
// @Slf4j
// public class TestController {

//     private final ChatClient chatClient;

//     public TestController(ChatClient chatClient) {
//         this.chatClient = chatClient;
//         log.info("TestController初始化成功，使用的ChatClient实现: {}", chatClient.getClass().getName());
//     }

//     @PostMapping("/chat")
//     public ChatResponse chat(@RequestBody ChatRequest request) {
//         String reply = chatClient.prompt()
//                 .user(request.getPrompt())
//                 .call()
//                 .content();
        
//         ChatResponse response = new ChatResponse();
//         response.setContent(reply);
//         response.setUserId(request.getUserId());
//         response.setRequestId(UUID.randomUUID().toString());
        
//         return response;
//     }

//     @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//     public Flux<String> stream(@RequestParam String prompt, 
//                                @RequestParam(required = false) String userId) {
//         return chatClient.prompt()
//                 .user(prompt)
//                 .stream()
//                 .content();
//     }
// } 