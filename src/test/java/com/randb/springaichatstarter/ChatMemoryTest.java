package com.randb.springaichatstarter;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试ChatMemory的正确实现
 */
@SpringBootTest
public class ChatMemoryTest {

    @Test
    public void testMessageWindowChatMemoryCreation() {
        // 测试MessageWindowChatMemory的创建
        ChatMemory chatMemory = MessageWindowChatMemory.builder().build();
        assertNotNull(chatMemory);
        
        // 测试带参数的创建
        ChatMemory chatMemoryWithMaxMessages = MessageWindowChatMemory.builder()
                .maxMessages(10)
                .build();
        assertNotNull(chatMemoryWithMaxMessages);
    }
    
    @Test
    public void testChatMemoryInterface() {
        ChatMemory chatMemory = MessageWindowChatMemory.builder().build();
        
        // 测试基本方法是否存在
        assertNotNull(chatMemory);
        
        // 测试get方法（这是之前出错的方法）
        String conversationId = "test-conversation";
        var messages = chatMemory.get(conversationId);
        assertNotNull(messages);
        assertTrue(messages.isEmpty()); // 新创建的内存应该是空的
    }
}
