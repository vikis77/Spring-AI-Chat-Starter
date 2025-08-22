package com.randb.springaichatstarter.controller;

import com.randb.springaichatstarter.core.ChatModelFactory;
import com.randb.springaichatstarter.core.ChatService;
import com.randb.springaichatstarter.dto.ChatRequest;
import com.randb.springaichatstarter.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/dev/api/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatModelFactory chatModelFactory;
    // private final ChatMemory chatMemory;

    /**
     * SSE流式接口
     */
    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatResponse> sse(@RequestParam String userId, 
                            @RequestParam String prompt, 
                            @RequestParam(defaultValue = "qwen") String model,
                            @RequestParam(required = false) String requestId) {
        ChatRequest req = new ChatRequest();
        req.setUserId(userId);
        req.setPrompt(prompt);
        req.setModel(model);
        req.setRequestId(requestId != null ? requestId : UUID.randomUUID().toString());
        
        log.info("SSE request received: {}", req);
        
        // 根据model参数动态选择聊天服务实现
        ChatService chatService = chatModelFactory.get(model);
        return chatService.streamReply(req);
    }
    
    /**
     * 同步问答接口
     */
    @PostMapping("/sync")
    public ChatResponse sync(@RequestBody ChatRequest request) {
        if (request.getRequestId() == null) {
            request.setRequestId(UUID.randomUUID().toString());
        }
        
        log.info("Sync request received: {}", request);
        
        ChatService chatService = chatModelFactory.get(request.getModel());
        ChatResponse reply = chatService.syncReply(request);
        
        ChatResponse response = new ChatResponse();
        response.setContent(reply.getContent());
        response.setUserId(request.getUserId());
        response.setRequestId(request.getRequestId());
        
        log.info("Sync response sent: requestId={}, contentLength={}", 
                response.getRequestId(), reply.getContent().length());
        
        return response;
    }

    /**
     * 查询历史对话记录
     */
    // @GetMapping("/history")
    // public String getHistory(String conversantId) {
    //     List<Message> message = chatMemory.get(conversantId);
    //     return "历史对话记录: " + message;
    // }
}
