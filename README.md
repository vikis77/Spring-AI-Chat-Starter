# Spring AI Chat Starter

🚀 **Spring Boot 大语言模型接入 Starter** - 一键集成多种AI模型，支持多协议通信

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.3-green.svg)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.0.0--M6-orange.svg)](https://spring.io/projects/spring-ai)

Spring Boot 系统一键接入大模型通信组件，支持通义千问、OpenAI等大模型，提供SSE流式响应、WebSocket和同步API接口。

## ✨ 特性

### 🤖 多模型支持
- **阿里通义千问** (DashScope) - 完整支持
- **OpenAI GPT** - 完整支持
- **易于扩展** - 支持添加其他大语言模型

### 🌐 多通信协议
- **HTTP REST API** - 同步请求/响应
- **SSE (Server-Sent Events)** - 流式响应
- **WebSocket** - 双向实时通信
- **RabbitMQ** - 异步消息队列

### ⚙️ 开箱即用
- **Spring Boot 自动配置** - 零配置启动
- **灵活配置** - 通过配置文件控制所有功能
- **服务降级** - 内置容错机制
- **对话记忆** - 支持上下文对话
- **前端示例** - 提供 HTML/JS 示例页面

## 📦 构建和部署

### 作为 Starter 使用（推荐）

1. **注释 Maven Plugin**：在 `pom.xml` 中注释掉 `spring-boot-maven-plugin`
   ```xml
   <!-- 这个plugin会把starter打包为可运行的springboot包，作为starter，不需要 -->
   <!-- <plugin>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-maven-plugin</artifactId>
       ...
   </plugin> -->
   ```

2. **安装到本地仓库**：
   ```bash
   mvn clean install
   ```

3. **在业务项目中使用**：
   ```xml
   <dependency>
       <groupId>com.randb</groupId>
       <artifactId>spring-ai-chat-starter</artifactId>
       <version>0.0.1-SNAPSHOT</version>
   </dependency>
   ```

### 独立运行（开发测试）

1. **取消注释 Maven Plugin**：在 `pom.xml` 中取消注释 `spring-boot-maven-plugin`

2. **运行项目**：
   ```bash
   mvn spring-boot:run
   ```

> **重要说明**：
> - **作为 Starter 使用**：注释掉 `spring-boot-maven-plugin`，安装到本地仓库供其他项目使用
> - **独立运行项目**：取消注释 `spring-boot-maven-plugin`，可以直接运行此项目进行开发测试

## � 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.randb</groupId>
    <artifactId>spring-ai-chat-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 2. 配置应用

复制 `src/main/resources/application-template.yml` 为 `application.yml` 并配置：

```yaml
spring:
  ai:
    # 通义千问配置
    dashscope:
      chat:
        options:
          model: qwen-turbo
      api-key: ${DASHSCOPE_API_KEY:your-api-key-here}
    # 模型配置
    model:
      chat: dashscope
    # Chat Starter 配置
    chat:
      enabled: true
      default-model: qwen
      websocket:
        enabled: true
      controller:
        enabled: true
```

### 3. 使用服务

```java
@Autowired
private ChatModelFactory chatModelFactory;

public void chatExample() {
    // 获取聊天服务
    ChatService chatService = chatModelFactory.get("qwen");

    // 创建请求
    ChatRequest request = new ChatRequest();
    request.setPrompt("你好");
    request.setUserId("user123");

    // 同步调用
    ChatResponse response = chatService.syncReply(request);
    System.out.println(response.getContent());
}
```

## 📖 使用指南

### HTTP REST API

#### 同步聊天
```bash
curl -X POST http://localhost:8080/api/chat/sync \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "prompt": "你好，介绍一下自己",
    "model": "qwen",
    "sessionId": "session123"
  }'
```

#### SSE 流式响应
```bash
curl -N http://localhost:8080/api/chat/sse?userId=user123&prompt=你好&model=qwen
```

### WebSocket

连接地址：`ws://localhost:8080/api/chat/ws`

发送消息格式：
```json
{
  "userId": "user123",
  "prompt": "你好，介绍一下自己",
  "model": "qwen",
  "sessionId": "session123"
}
```

### 编程接口

```java
@Autowired
private ChatModelFactory chatModelFactory;

public void chatExample() {
    // 获取聊天服务
    ChatService chatService = chatModelFactory.get("qwen");

    // 创建请求
    ChatRequest request = new ChatRequest();
    request.setPrompt("你好");
    request.setUserId("user123");
    request.setSessionId("session123");

    // 同步调用
    ChatResponse response = chatService.syncReply(request);
    System.out.println(response.getContent());

    // 流式调用
    chatService.streamReply(request)
        .subscribe(chunk -> System.out.print(chunk.getContent()));
}
```

## ⚙️ 配置选项

### 基础配置
| 配置项 | 描述 | 默认值 | 必需 |
|--------|------|--------|------|
| `spring.ai.chat.enabled` | 是否启用 AI 聊天功能 | `true` | 否 |
| `spring.ai.chat.default-model` | 默认模型名称 | `qwen` | 否 |
| `spring.ai.dashscope.api-key` | 通义千问 API Key | - | 是* |
| `spring.ai.openai.api-key` | OpenAI API Key | - | 是* |

*至少需要配置一个模型的API Key

### WebSocket 配置
| 配置项 | 描述 | 默认值 |
|--------|------|--------|
| `spring.ai.chat.websocket.enabled` | 是否启用 WebSocket | `true` |
| `spring.ai.chat.websocket.path` | WebSocket 路径 | `/api/chat/ws` |

### RabbitMQ 配置
| 配置项 | 描述 | 默认值 |
|--------|------|--------|
| `spring.ai.chat.rabbitmq.enabled` | 是否启用 RabbitMQ | `false` |
| `spring.ai.chat.rabbitmq.exchange` | 交换机名称 | `chat.exchange` |
| `spring.ai.chat.rabbitmq.queue` | 队列名称 | `chat.queue` |

### REST API 配置
| 配置项 | 描述 | 默认值 |
|--------|------|--------|
| `spring.ai.chat.controller.enabled` | 是否启用 REST 控制器 | `true` |
| `spring.ai.chat.controller.base-path` | API 基础路径 | `/api/chat` |

## 🔧 开发指南

### 添加新的AI模型

1. 实现 `ChatService` 接口：

```java
@Component("your-model")
public class YourModelChatServiceImpl implements ChatService {
    @Override
    public Flux<ChatResponse> streamReply(ChatRequest request) {
        // 实现流式响应
    }

    @Override
    public ChatResponse syncReply(ChatRequest request) {
        // 实现同步响应
    }
}
```

2. 在配置中注册模型，使用 chatModelFactory.get("your-model") 获取服务。

## 🏗️ 项目架构

```
spring-ai-chat-starter/
├── src/main/java/com/randb/springaichatstarter/
│   ├── autoconfigure/          # 自动配置类
│   │   ├── SpringAiChatAutoConfiguration.java
│   │   ├── DashScopeModelAutoConfig.java
│   │   ├── WebSocketConfiguration.java
│   │   └── RabbitMQConfiguration.java
│   ├── core/                   # 核心服务
│   │   ├── ChatService.java           # 统一聊天接口
│   │   ├── ChatModelFactory.java      # 模型工厂
│   │   ├── QwenChatServiceImpl.java    # 通义千问实现
│   │   └── DefaultQwenChatServiceImpl.java # 降级实现
│   ├── controller/             # REST API控制器
│   │   └── ChatController.java
│   ├── dto/                    # 数据传输对象
│   │   ├── ChatRequest.java
│   │   └── ChatResponse.java
│   ├── websocket/              # WebSocket处理器
│   │   └── ChatWebSocketHandler.java
│   ├── mq/                     # 消息队列服务
│   │   └── ChatMessageService.java
│   └── util/                   # 工具类
│       └── ChatResponseUtil.java
├── src/main/resources/
│   ├── META-INF/spring/        # 自动配置
│   ├── static/                 # 示例页面
│   └── application-template.yml # 配置模板
└── pom.xml
```

### 设计模式

- **工厂模式**：`ChatModelFactory` 动态选择模型实现
- **适配器模式**：`ChatClientAdapter` 适配不同模型接口
- **策略模式**：通过配置选择不同的通信协议
- **降级模式**：提供默认实现确保服务可用性

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 🙏 致谢

- [Spring AI](https://spring.io/projects/spring-ai) - 提供AI集成框架
- [阿里云DashScope](https://dashscope.aliyun.com/) - 通义千问API服务
- [OpenAI](https://openai.com/) - GPT模型API服务

## 📞 联系方式

- 作者：randb
- 项目链接：[https://github.com/your-username/spring-ai-chat-starter](https://github.com/your-username/spring-ai-chat-starter)
- 开发者博客: [https://luckyiur.com](https://luckyiur.com)
- 发送邮件至: qin2607994895@gmail.com

---

⭐ 如果这个项目对你有帮助，请给个 Star！