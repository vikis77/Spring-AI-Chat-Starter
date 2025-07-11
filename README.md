# Spring AI Chat Starter

  Spring Boot 系统一键接入大模型通信组件，支持通义千问、OpenAI等大模型，提供SSE流式响应、WebSocket和同步API接口。

  ## 特性

  - 支持多种通信方式
    - HTTP 同步请求/响应
    - SSE 流式响应
    - WebSocket 双向通信
    - RabbitMQ 消息队列
  - 支持多种大语言模型
    - 阿里通义千问 (DashScope)
    - OpenAI (GPT)
    - 易于扩展其他模型
  - 自动配置
    - 基于 Spring Boot 自动配置机制
    - 可通过配置文件灵活控制功能启用/禁用
  - 前端示例
    - 提供 HTML/JS 示例页面

  ## 项目结构(大致架构)
  ```markdown
  spring-ai-chat-starter/
  ├── src/main/java/com/randb/springaichatstarter/
  │ ├── autoconfigure/ # 自动配置类 
  │ │ ├── ChatServiceAutoConfiguration.java # 聊天服务自动配置
  │ │ ├── QwenServiceAutoConfiguration.java # 通义千问服务自动配置
  │ │ ├── SpringAiChatAutoConfiguration.java # 主自动配置类
  │ │ ├── SpringAiChatProperties.java # 配置属性类
  │ │ ├── WebControllerConfiguration.java # Web控制器配置
  │ │ ├── WebSocketConfiguration.java # WebSocket配置
  │ │ └── RabbitMQConfiguration.java # RabbitMQ配置
  │ ├── config/ # 配置类
  │ │ ├── DashScopeModelAutoConfig.java # 通义千问模型配置
  │ │ ├── ServiceConfig.java # 服务配置
  │ │ └── WebConfig.java # Web配置
  │ ├── controller/ # REST API控制器（开发使用）
  │ │ └── ChatController.java # 聊天API控制器
  │ ├── core/ # 核心服务实现
  │ │ ├── ChatModelFactory.java # 聊天模型工厂
  │ │ ├── ChatService.java # 聊天服务接口
  │ │ ├── DefaultQwenChatServiceImpl.java # 默认通义千问实现
  │ │ └── QwenChatServiceImpl.java # 真实通义千问实现
  │ ├── dto/ # 数据传输对象
  │ │ ├── ChatRequest.java # 聊天请求
  │ │ └── ChatResponse.java # 聊天响应
  │ ├── mq/ # 消息队列相关
  │ │ └── ChatMessageService.java # 聊天消息服务
  │ └── websocket/ # WebSocket相关
  │ ├── ChatWebSocketHandler.java # WebSocket处理器
  │ └── WebSocketSessionManager.java # WebSocket会话管理器
  ├── src/main/resources/
  │ ├── META-INF/spring/
  │ │ └── org.springframework.boot.autoconfigure.AutoConfiguration.imports # 自动配置导入
  │ ├── static/ # 静态资源
  │ │ ├── index.html # SSE示例页面
  │ │ └── websocket.html # WebSocket示例页面
  │ └── application.yml # 默认配置文件
  └── pom.xml # Maven配置
  ```
  该项目结构遵循以下设计原则：

  1. **模块化设计**：按功能将代码组织到不同包中
  2. **自动配置**：利用Spring Boot自动配置机制简化集成
  3. **服务抽象**：通过接口定义标准化服务行为
  4. **工厂模式**：使用ChatModelFactory实现动态模型选择
  5. **适配器模式**：在ChatModelFactory中使用ChatClientAdapter连接不同接口
  6. **降级策略**：提供DefaultQwenChatServiceImpl作为服务降级方案
  7. **多通道通信**：支持REST、SSE、WebSocket和消息队列


  ## 快速开始

  ### 添加依赖

  ```xml
  <dependency>
      <groupId>com.randb</groupId>
      <artifactId>spring-ai-chat-starter</artifactId>
      <version>0.0.1-SNAPSHOT</version>
  </dependency>
  ```

  ### 配置应用

  在 `application.yml` 中添加配置：

  ```yaml
  spring:
    ai:
      dashscope:
        chat:
          options:
            model: qwen-turbo
        api-key: your-api-key-here
      model:
        chat: dashscope
      chat:
        enabled: true
        default-model: qwen
        websocket:
          enabled: true
        rabbitmq:
          enabled: false
  ```

  ### 访问 API

  #### HTTP 同步请求

  同步调用示例
  ```java
  @Service
  public class MyAiService {
      private final ChatModelFactory chatModelFactory;
      
      public MyAiService(ChatModelFactory chatModelFactory) {
          this.chatModelFactory = chatModelFactory;
      }
      
      public String getAiResponse(String prompt) {
          ChatRequest request = new ChatRequest();
          request.setPrompt(prompt);
          request.setModel("qwen");
          
          ChatService chatService = chatModelFactory.get(request.getModel());
          return chatService.syncReply(request);
      }
  }
  ```

  请求体:
  ```json
  {
    "userId": "user-123",
    "prompt": "你好，请介绍一下自己",
    "model": "qwen"
  }
  ```

  #### SSE 流式响应

  流式调用示例
  ```java
  @RestController
  @RequestMapping("/my-api/chat")
  public class MyAiController {
      private final ChatModelFactory chatModelFactory;
      
      public MyAiController(ChatModelFactory chatModelFactory) {
          this.chatModelFactory = chatModelFactory;
      }
      
      @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
      public Flux<String> streamChat(@RequestParam String prompt) {
          ChatRequest request = new ChatRequest();
          request.setPrompt(prompt);
          request.setModel("qwen");
          
          ChatService chatService = chatModelFactory.get(request.getModel());
          return chatService.streamReply(request);
      }
  }
  ```

  #### WebSocket

  引入starter之后，在application.yml中配置spring.ai.chat.websocket.enabled: true即可。请求ws://localhost:your-server-port/api/chat/ws

  发送消息:
  ```json
  {
    "requestId": null,
    "userId": null,
    "prompt": "你是谁",
    "model": "qwen",
    "sessionId": null,
    "replyTo": null,
    "stream": null
  }
  ```

  #### 消息队列

  消息队列使用示例
  ```java
  @Service
  public class MyMqService {
      private final ChatMessageService chatMessageService;
      
      public MyMqService(ChatMessageService chatMessageService) {
          this.chatMessageService = chatMessageService;
      }
      
      public String sendChatRequest(String prompt) {
          ChatRequest request = new ChatRequest();
          request.setPrompt(prompt);
          request.setModel("qwen");
          
          return chatMessageService.sendChatRequest(request);
      }
      
      public ChatResponse waitForResponse(String requestId) {
          return chatMessageService.waitForResponse(requestId, 10000);
      }
  }
  ```

  ## 配置选项

  | 配置项 | 描述 | 默认值 |
  |--------|------|--------|
  | spring.ai.chat.enabled | 是否启用 AI 聊天功能 | true |
  | spring.ai.chat.default-model | 默认模型名称 | qwen |
  | spring.ai.chat.websocket.enabled | 是否启用 WebSocket | true |
  | spring.ai.chat.websocket.path | WebSocket 路径 | /api/chat/ws |
  | spring.ai.chat.rabbitmq.enabled | 是否启用 RabbitMQ | false |
  | spring.ai.chat.rabbitmq.exchange | 交换机名称 | chat.exchange |
  | spring.ai.chat.rabbitmq.queue | 队列名称 | chat.queue |
  | spring.ai.chat.rabbitmq.routing-key | 路由键 | chat.request |
  | spring.ai.chat.controller.enabled | 是否启用 REST 控制器 | true |
  | spring.ai.chat.controller.base-path | API 基础路径 | /api/chat |

  ## 添加新的LLM模型

  业务服务可以通过以下方式添加新模型：实现 `ChatService` 接口并使用 Spring 的 `@Service` 注解命名：
  ```java
  @Service("myCustomModel")
  public class MyCustomModelService implements ChatService {
      @Override
      public Flux<String> streamReply(ChatRequest request) {
          // 实现流式回复
      }

      @Override
      public String syncReply(ChatRequest request) {
          // 实现同步回复
      }
  }
  ```

  ## 前端示例

  启动应用后访问：
  - `http://localhost:8080/index.html` - SSE 和同步请求示例
  - `http://localhost:8080/websocket.html` - WebSocket 示例

  ## 开源协议

  MIT


  ## 使用问题

  1. 解决SSE响应中文显示为问号的问题。
  在使用Spring AI Chat Starter时，如果遇到SSE（Server-Sent Events）响应中文显示为问号的问题，主要是由于以下原因：
    Spring Boot版本差异：spring-ai-chat-starter使用Spring Boot 3.5.3，而您的项目可能使用不同版本
    WebFlux和WebMVC配置冲突：同时存在两种Web框架配置会导致编码处理不一致
    字符编码设置不正确：SSE响应没有正确设置UTF-8编码
  解决方案
  为解决这些问题，需要添加以下三个关键配置：
  1.1. SSE消息转换器配置
  创建SseMessageConverterConfig.java，确保SSE响应使用UTF-8编码：
  ```java
  @Configuration
  @Slf4j
  public class SseMessageConverterConfig implements BeanPostProcessor {
      
      @Bean
      @Primary
      public StringHttpMessageConverter sseStringHttpMessageConverter() {
          StringHttpMessageConverter converter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
          
          // 设置支持的媒体类型，重点是text/event-stream
          MediaType textEventStream = new MediaType("text", "event-stream", StandardCharsets.UTF_8);
          converter.setSupportedMediaTypes(Arrays.asList(
              textEventStream,
              new MediaType("application", "json", StandardCharsets.UTF_8),
              new MediaType("text", "plain", StandardCharsets.UTF_8)
          ));
          
          return converter;
      }
      
      @Override
      public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
          if (bean instanceof RequestMappingHandlerAdapter) {
              RequestMappingHandlerAdapter adapter = (RequestMappingHandlerAdapter) bean;
              
              // 设置所有StringHttpMessageConverter使用UTF-8编码
              adapter.getMessageConverters().stream()
                  .filter(converter -> converter instanceof StringHttpMessageConverter)
                  .map(converter -> (StringHttpMessageConverter) converter)
                  .forEach(converter -> {
                      converter.setDefaultCharset(StandardCharsets.UTF_8);
                  });
          }
          return bean;
      }
  }
  ```

  1.2. SSE Accept头过滤器
  创建SseAcceptHeaderFilter.java，解决"No acceptable representation"错误：
  ```java
  @Component
  @Order(Ordered.HIGHEST_PRECEDENCE + 1)
  @Slf4j
  public class SseAcceptHeaderFilter implements Filter {

      @Override
      public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
              throws IOException, ServletException {
          
          HttpServletRequest httpRequest = (HttpServletRequest) request;
          String path = httpRequest.getRequestURI();
          
          // 检查是否是SSE请求
          if (path.contains("/api/chat/sse")) {
              // 包装请求，修改Accept头
              HttpServletRequest wrappedRequest = new HttpServletRequestWrapper(httpRequest) {
                  private final Map<String, String> headerMap = new HashMap<>();
                  
                  {
                      // 设置Accept头为text/event-stream
                      headerMap.put("Accept", MediaType.TEXT_EVENT_STREAM_VALUE);
                  }
                  
                  @Override
                  public String getHeader(String name) {
                      String headerValue = headerMap.get(name);
                      if (headerValue != null) {
                          return headerValue;
                      }
                      return super.getHeader(name);
                  }
                  
                  // 其他必要的重写方法...
              };
              
              chain.doFilter(wrappedRequest, response);
          } else {
              chain.doFilter(request, response);
          }
      }
  }
  ```

  1.3. 修改SSE接口实现
  在Controller中确保SSE接口正确处理UTF-8编码：
  ```java
  @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<String> sse(@RequestParam String userId, 
                        @RequestParam String prompt, 
                        @RequestParam(defaultValue = "qwen") String model,
                        @RequestParam(required = false) String requestId) {
      ChatRequest req = new ChatRequest();
      req.setUserId(userId);
      req.setPrompt(prompt);
      req.setModel(model);
      req.setRequestId(requestId != null ? requestId : UUID.randomUUID().toString());
      
      ChatService chatService = chatModelFactory.get(model);
      
      return chatService.streamReply(req)
              .map(text -> {
                  // 确保UTF-8编码
                  byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
                  String result = new String(bytes, StandardCharsets.UTF_8);
                  return result;
              });
  }
  ```