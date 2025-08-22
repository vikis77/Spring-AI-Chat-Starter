package com.randb.springaichatstarter;

import com.randb.springaichatstarter.core.DefaultQwenChatServiceImpl;
import com.randb.springaichatstarter.dto.ChatRequest;
import com.randb.springaichatstarter.dto.ChatResponse;
import com.randb.springaichatstarter.util.ChatResponseUtil;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ChatResponse 统一响应对象测试
 */
public class ChatResponseTest {

    @Test
    public void testChatResponseUtil() {
        ChatRequest request = new ChatRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setUserId("testUser");
        request.setPrompt("测试提示");

        // 测试创建消息响应
        ChatResponse messageResponse = ChatResponseUtil.createMessage(request, "测试内容");
        assertEquals("message", messageResponse.getType());
        assertEquals(request.getRequestId(), messageResponse.getRequestId());
        assertEquals(request.getUserId(), messageResponse.getUserId());
        assertEquals("测试内容", messageResponse.getContent());
        assertTrue(messageResponse.getTimestamp() > 0);

        // 测试创建错误响应
        ChatResponse errorResponse = ChatResponseUtil.createError(request, "错误信息");
        assertEquals("error", errorResponse.getType());
        assertEquals("错误信息", errorResponse.getContent());

        // 测试创建完成响应
        ChatResponse completedResponse = ChatResponseUtil.createCompleted(request);
        assertEquals("completed", completedResponse.getType());
        assertEquals("[DONE]", completedResponse.getContent());

        // 测试创建心跳响应
        ChatResponse pongResponse = ChatResponseUtil.createPong();
        assertEquals("pong", pongResponse.getType());
        assertEquals("pong", pongResponse.getContent());
    }

    @Test
    public void testDefaultQwenChatServiceImpl() {
        DefaultQwenChatServiceImpl service = new DefaultQwenChatServiceImpl();
        
        ChatRequest request = new ChatRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setUserId("testUser");
        request.setPrompt("你好");

        // 测试同步响应
        ChatResponse syncResponse = service.syncReply(request);
        assertNotNull(syncResponse);
        assertEquals("message", syncResponse.getType());
        assertEquals(request.getRequestId(), syncResponse.getRequestId());
        assertEquals(request.getUserId(), syncResponse.getUserId());
        assertTrue(syncResponse.getContent().contains("这是默认的通义千问回复（同步）"));
        assertTrue(syncResponse.getTimestamp() > 0);

        // 测试流式响应
        Flux<ChatResponse> streamResponse = service.streamReply(request);
        AtomicReference<ChatResponse> receivedResponse = new AtomicReference<>();

        streamResponse.subscribe(
            response -> receivedResponse.set(response),
            error -> fail("流式响应不应该出错: " + error.getMessage()),
            () -> {
                ChatResponse response = receivedResponse.get();
                assertNotNull(response);
                assertEquals("message", response.getType());
                assertEquals(request.getRequestId(), response.getRequestId());
                assertEquals(request.getUserId(), response.getUserId());
                assertTrue(response.getContent().contains("这是默认的通义千问回复（流式）"));
                assertTrue(response.getTimestamp() > 0);
            }
        );

        // 等待一下确保异步操作完成
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    public void testChatResponseSerialization() {
        ChatRequest request = new ChatRequest();
        request.setRequestId("test-request-id");
        request.setUserId("test-user");
        request.setPrompt("测试");

        ChatResponse response = ChatResponseUtil.createMessage(request, "测试响应");
        
        // 验证所有字段都已正确设置
        assertNotNull(response.getType());
        assertNotNull(response.getRequestId());
        assertNotNull(response.getUserId());
        assertNotNull(response.getContent());
        assertTrue(response.getTimestamp() > 0);
        
        // 验证字段值
        assertEquals("message", response.getType());
        assertEquals("test-request-id", response.getRequestId());
        assertEquals("test-user", response.getUserId());
        assertEquals("测试响应", response.getContent());
    }

    @Test
    public void testChatResponseWithNullRequest() {
        // 测试当request为null时的处理
        ChatResponse response = ChatResponseUtil.createMessage(null, "测试内容");
        assertEquals("message", response.getType());
        assertNull(response.getRequestId());
        assertNull(response.getUserId());
        assertEquals("测试内容", response.getContent());
        assertTrue(response.getTimestamp() > 0);
    }
}
