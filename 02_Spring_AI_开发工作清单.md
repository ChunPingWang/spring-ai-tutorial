# Spring AI 开发工作清单
## Coding Agent 开发与验证指南

**版本**: 1.0 | **类型**: 可执行任务清单 | **用途**: AI Agent辅助开发

---

## 📋 项目初始化

### Task 1.1: 项目骨架搭建
**优先级**: 🔴 高 | **依赖**: 无 | **预计时间**: 2小时

**描述**: 创建基础的Spring Boot项目结构

**验收标准**:
- [ ] 项目成功初始化，使用Spring Boot 3.x
- [ ] Maven/Gradle依赖完整配置
- [ ] 项目结构符合Maven标准目录
- [ ] 能够成功编译和运行

**实现步骤**:

```bash
# 使用Spring Initializr或Maven Archetype
mvn archetype:generate \
  -DgroupId=com.example.ai \
  -DartifactId=spring-ai-app \
  -DarchetypeArtifactId=maven-archetype-quickstart

# 或使用curl调用Spring Initializr
curl https://start.spring.io/starter.zip \
  -d dependencies=web,spring-ai-openai \
  -d type=maven-project \
  -o spring-ai-app.zip
```

**配置文件**:
```xml
<!-- pom.xml -->
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-bom</artifactId>
            <version>1.0.0-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-spring-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
    </dependency>
</dependencies>
```

**相关文件**:
- `pom.xml` - Maven配置
- `src/main/java/Application.java` - 入口类
- `src/main/resources/application.yml` - 应用配置

---

### Task 1.2: 环境变量配置
**优先级**: 🔴 高 | **依赖**: Task 1.1 | **预计时间**: 1小时

**描述**: 配置LLM API密钥和数据库连接

**验收标准**:
- [ ] 环境变量正确配置（支持本地.env和云平台）
- [ ] 支持多个LLM提供商（OpenAI、Anthropic、Bedrock）
- [ ] 数据库连接成功建立
- [ ] 敏感信息未hardcode

**实现步骤**:

```yaml
# application.yml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      base-url: https://api.openai.com/v1
      model: gpt-4
    anthropic:
      api-key: ${ANTHROPIC_API_KEY}
    bedrock:
      region: ${AWS_REGION:us-east-1}
  
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/spring_ai_db}
    username: ${DB_USER:postgres}
    password: ${DB_PASSWORD:postgres}
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect

logging:
  level:
    org.springframework.ai: DEBUG
    org.springframework.web: INFO
```

```java
// config/ApplicationProperties.java
@Configuration
@ConfigurationProperties(prefix = "spring.ai")
@Data
public class AIProperties {
    private OpenAI openAI;
    private Anthropic anthropic;
    private AWS aws;
    
    @Data
    public static class OpenAI {
        private String apiKey;
        private String model;
        private double temperature;
    }
}
```

**验证脚本**:

```java
// src/test/java/config/ConfigurationTest.java
@SpringBootTest
class ConfigurationTest {
    
    @Test
    void testOpenAIConfiguration() {
        assertNotNull(openAiClient);
        // 尝试简单的API调用
    }
    
    @Test
    void testDatabaseConnection() {
        assertNotNull(dataSource);
        // 验证数据库连接
    }
}
```

---

## 🤖 第一阶段: 基础功能实现

### Task 2.1: ChatClient 基础实现
**优先级**: 🔴 高 | **依赖**: Task 1.2 | **预计时间**: 3小时

**描述**: 实现Spring AI ChatClient的基本功能

**验收标准**:
- [ ] ChatClient Bean成功创建
- [ ] 实现简单文本对话
- [ ] 支持系统提示词
- [ ] 支持温度、Token等参数配置
- [ ] 单元测试覆盖率>80%

**实现步骤**:

```java
// service/ChatService.java
@Service
@Slf4j
public class ChatService {
    
    private final ChatClient chatClient;
    
    public ChatService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }
    
    /**
     * 简单聊天
     * @param message 用户输入
     * @return AI回复
     */
    public String simpleChat(String message) {
        return chatClient.prompt()
            .user(message)
            .call()
            .content();
    }
    
    /**
     * 带系统提示的聊天
     * @param systemPrompt 系统提示
     * @param userMessage 用户消息
     * @return AI回复
     */
    public String chatWithSystemPrompt(String systemPrompt, String userMessage) {
        return chatClient.prompt()
            .system(systemPrompt)
            .user(userMessage)
            .call()
            .content();
    }
    
    /**
     * 可配置的聊天
     */
    public String chatWithOptions(String message, ChatOptions options) {
        return chatClient.prompt()
            .user(message)
            .options(o -> {
                if (options != null) {
                    if (options.getTemperature() != null) {
                        o.temperature(options.getTemperature());
                    }
                    if (options.getMaxTokens() != null) {
                        o.maxTokens(options.getMaxTokens());
                    }
                }
            })
            .call()
            .content();
    }
}
```

```java
// controller/ChatController.java
@RestController
@RequestMapping("/api/chat")
@Slf4j
@RequiredArgsConstructor
public class ChatController {
    
    private final ChatService chatService;
    
    @PostMapping("/simple")
    public ResponseEntity<ChatResponse> simpleChat(@RequestBody ChatRequest request) {
        String response = chatService.simpleChat(request.getMessage());
        return ResponseEntity.ok(new ChatResponse(response));
    }
    
    @PostMapping("/with-system-prompt")
    public ResponseEntity<ChatResponse> chatWithSystemPrompt(
            @RequestBody ChatWithSystemPromptRequest request) {
        
        String response = chatService.chatWithSystemPrompt(
            request.getSystemPrompt(),
            request.getUserMessage()
        );
        return ResponseEntity.ok(new ChatResponse(response));
    }
}
```

```java
// dto/ChatRequest.java
@Data
public class ChatRequest {
    @NotBlank
    private String message;
}

@Data
public class ChatWithSystemPromptRequest {
    @NotBlank
    private String systemPrompt;
    
    @NotBlank
    private String userMessage;
}

@Data
public class ChatResponse {
    private String response;
}
```

**单元测试**:

```java
// test/service/ChatServiceTest.java
@SpringBootTest
class ChatServiceTest {
    
    @Autowired
    private ChatService chatService;
    
    @Test
    void testSimpleChat() {
        String response = chatService.simpleChat("Hello, AI!");
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }
    
    @Test
    void testChatWithSystemPrompt() {
        String response = chatService.chatWithSystemPrompt(
            "You are a helpful assistant.",
            "What is 2+2?"
        );
        assertNotNull(response);
        assertTrue(response.contains("4"));
    }
}
```

**集成测试**:

```java
// test/integration/ChatIntegrationTest.java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChatIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void testChatEndpoint() {
        ChatRequest request = new ChatRequest();
        request.setMessage("Hello");
        
        ResponseEntity<ChatResponse> response = restTemplate.postForEntity(
            "/api/chat/simple",
            request,
            ChatResponse.class
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
```

---

### Task 2.2: REST API 设计与实现
**优先级**: 🟡 中 | **依赖**: Task 2.1 | **预计时间**: 2小时

**描述**: 设计企业级REST API，支持异步、流式响应

**验收标准**:
- [ ] API遵循RESTful原则
- [ ] 支持异步调用（CompletableFuture）
- [ ] 支持流式响应（Server-Sent Events）
- [ ] 统一的错误处理
- [ ] API文档完整（Swagger/OpenAPI）

**实现步骤**:

```java
// controller/AIController.java
@RestController
@RequestMapping("/api/v1/ai")
@Slf4j
@RequiredArgsConstructor
public class AIController {
    
    private final ChatService chatService;
    
    /**
     * 异步聊天端点
     */
    @PostMapping("/chat/async")
    public CompletableFuture<ResponseEntity<ChatResponse>> asyncChat(
            @RequestBody ChatRequest request) {
        
        return CompletableFuture.supplyAsync(() -> {
            String response = chatService.simpleChat(request.getMessage());
            return ResponseEntity.ok(new ChatResponse(response));
        });
    }
    
    /**
     * 流式聊天端点
     */
    @GetMapping("/chat/stream")
    public SseEmitter streamChat(@RequestParam String message) {
        SseEmitter emitter = new SseEmitter(60000L);
        
        // 在后台线程处理
        new Thread(() -> {
            try {
                // 模拟流式响应
                String[] words = chatService.simpleChat(message).split(" ");
                for (String word : words) {
                    emitter.send(SseEmitter.event()
                        .id(UUID.randomUUID().toString())
                        .data(word + " ")
                        .reconnectTime(100));
                    Thread.sleep(100);
                }
                emitter.complete();
            } catch (IOException | InterruptedException e) {
                emitter.completeWithError(e);
            }
        }).start();
        
        return emitter;
    }
}
```

```yaml
# OpenAPI/Swagger 配置
springdoc:
  swagger-ui:
    enabled: true
    path: /swagger-ui.html
  api-docs:
    path: /v3/api-docs
```

---

### Task 2.3: 错误处理与日志
**优先级**: 🟡 中 | **依赖**: Task 2.1 | **预计时间**: 2小时

**描述**: 实现统一的异常处理和结构化日志

**验收标准**:
- [ ] 所有异常都被妥善处理
- [ ] 错误响应格式统一
- [ ] 支持结构化日志（JSON格式）
- [ ] 包含request tracing ID
- [ ] 敏感信息被掩盖

**实现步骤**:

```java
// exception/GlobalExceptionHandler.java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(AIServiceException.class)
    public ResponseEntity<ErrorResponse> handleAIServiceException(
            AIServiceException ex, 
            HttpServletRequest request) {
        
        String traceId = MDC.get("trace_id");
        log.error("AI Service Error [{}]: {}", traceId, ex.getMessage(), ex);
        
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse.builder()
                .traceId(traceId)
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build());
    }
    
    @ExceptionHandler(APIKeyMissingException.class)
    public ResponseEntity<ErrorResponse> handleAPIKeyMissing(
            APIKeyMissingException ex) {
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.builder()
                .message("API配置错误")
                .timestamp(LocalDateTime.now())
                .build());
    }
}
```

```java
// aspect/LoggingAspect.java
@Aspect
@Component
@Slf4j
public class LoggingAspect {
    
    @Before("execution(* com.example.ai.controller.*.*(..))")
    public void logBefore(JoinPoint joinPoint) {
        String traceId = UUID.randomUUID().toString();
        MDC.put("trace_id", traceId);
        
        log.info("Incoming request: {} with traceId: {}",
            joinPoint.getSignature().getName(),
            traceId);
    }
    
    @AfterReturning(value = "execution(* com.example.ai.controller.*.*(..))",
                    returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        log.info("Request completed: {}",
            joinPoint.getSignature().getName());
    }
}
```

```yaml
# logback-spring.xml 日志配置
<configuration>
    <property name="LOG_FILE" value="${LOG_FILE:-${LOG_PATH:-${LOG_TEMP:-${java.io.tmpdir:-/tmp}}/}spring.log}"/>
    
    <appender name="JSON_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_FILE}</file>
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <customFields>{"app":"spring-ai"}</customFields>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="JSON_FILE"/>
    </root>
</configuration>
```

---

## 💾 第二阶段: 对话与记忆管理

### Task 3.1: 会话管理实现
**优先级**: 🔴 高 | **依赖**: Task 2.1 | **预计时间**: 4小时

**描述**: 实现多轮对话的会话管理和历史记录

**验收标准**:
- [ ] 支持创建和管理多个对话会话
- [ ] 会话历史准确保存
- [ ] 支持会话继续和清除
- [ ] 会话数据持久化到数据库
- [ ] 支持Session Timeout

**实现步骤**:

```java
// entity/ConversationSession.java
@Entity
@Table(name = "conversation_sessions")
@Data
@NoArgsConstructor
public class ConversationSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private String userId;
    
    @Column(nullable = false)
    private String title;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    @Enumerated(EnumType.STRING)
    private SessionStatus status;
    
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
    private List<ConversationMessage> messages = new ArrayList<>();
    
    private int messageCount;
    private long totalTokens;
}

@Entity
@Table(name = "conversation_messages")
@Data
@NoArgsConstructor
public class ConversationMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private ConversationSession session;
    
    @Enumerated(EnumType.STRING)
    private MessageRole role;  // USER, ASSISTANT, SYSTEM
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @CreationTimestamp
    private LocalDateTime timestamp;
    
    private int tokenCount;
}

enum MessageRole {
    USER, ASSISTANT, SYSTEM
}

enum SessionStatus {
    ACTIVE, PAUSED, CLOSED
}
```

```java
// repository/ConversationRepository.java
@Repository
public interface ConversationSessionRepository extends JpaRepository<ConversationSession, String> {
    List<ConversationSession> findByUserId(String userId);
    List<ConversationSession> findByUserIdAndStatus(String userId, SessionStatus status);
}

@Repository
public interface ConversationMessageRepository extends JpaRepository<ConversationMessage, String> {
    List<ConversationMessage> findBySessionIdOrderByTimestampAsc(String sessionId);
}
```

```java
// service/ConversationService.java
@Service
@Slf4j
@RequiredArgsConstructor
public class ConversationService {
    
    private final ConversationSessionRepository sessionRepository;
    private final ConversationMessageRepository messageRepository;
    private final ChatService chatService;
    
    /**
     * 创建新对话会话
     */
    public ConversationSession createSession(String userId, String title) {
        ConversationSession session = new ConversationSession();
        session.setUserId(userId);
        session.setTitle(title);
        session.setStatus(SessionStatus.ACTIVE);
        session.setMessageCount(0);
        session.setTotalTokens(0);
        
        return sessionRepository.save(session);
    }
    
    /**
     * 继续对话
     */
    public ConversationMessage continueConversation(String sessionId, String userMessage) {
        ConversationSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        
        // 保存用户消息
        ConversationMessage userMsg = new ConversationMessage();
        userMsg.setSession(session);
        userMsg.setRole(MessageRole.USER);
        userMsg.setContent(userMessage);
        userMsg.setTokenCount(estimateTokens(userMessage));
        messageRepository.save(userMsg);
        
        // 构建对话历史上下文
        List<ConversationMessage> history = messageRepository
            .findBySessionIdOrderByTimestampAsc(sessionId);
        
        // 生成AI响应
        String assistantResponse = generateResponse(session, history, userMessage);
        
        // 保存AI响应
        ConversationMessage assistantMsg = new ConversationMessage();
        assistantMsg.setSession(session);
        assistantMsg.setRole(MessageRole.ASSISTANT);
        assistantMsg.setContent(assistantResponse);
        assistantMsg.setTokenCount(estimateTokens(assistantResponse));
        messageRepository.save(assistantMsg);
        
        // 更新会话统计
        session.setMessageCount(session.getMessageCount() + 2);
        session.setTotalTokens(session.getTotalTokens() + 
            userMsg.getTokenCount() + assistantMsg.getTokenCount());
        session.setUpdatedAt(LocalDateTime.now());
        sessionRepository.save(session);
        
        return assistantMsg;
    }
    
    /**
     * 获取对话历史
     */
    public List<ConversationMessage> getConversationHistory(String sessionId) {
        return messageRepository.findBySessionIdOrderByTimestampAsc(sessionId);
    }
    
    /**
     * 清除对话
     */
    public void clearConversation(String sessionId) {
        ConversationSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        
        messageRepository.deleteAll(session.getMessages());
        session.setMessageCount(0);
        session.setTotalTokens(0);
        sessionRepository.save(session);
    }
    
    private String generateResponse(ConversationSession session,
                                   List<ConversationMessage> history,
                                   String userMessage) {
        // 构建Prompt，包含历史记录
        var prompt = chatService.createPrompt();
        
        // 添加系统消息
        prompt.system(buildSystemPrompt(session));
        
        // 添加历史消息
        for (ConversationMessage msg : history) {
            if (msg.getRole() == MessageRole.USER) {
                prompt.user(msg.getContent());
            } else if (msg.getRole() == MessageRole.ASSISTANT) {
                prompt.assistant(msg.getContent());
            }
        }
        
        // 添加当前用户消息
        prompt.user(userMessage);
        
        return prompt.call().content();
    }
    
    private String buildSystemPrompt(ConversationSession session) {
        return "你是一个有帮助的AI助手。" +
               "你正在与用户进行对话: " + session.getTitle() + " 。" +
               "记住对话历史，保持一致的角色和知识。";
    }
    
    private int estimateTokens(String text) {
        // 简单估算：大约每4个字符1个token
        return (text.length() / 4) + 1;
    }
}
```

```java
// controller/ConversationController.java
@RestController
@RequestMapping("/api/v1/conversations")
@Slf4j
@RequiredArgsConstructor
public class ConversationController {
    
    private final ConversationService conversationService;
    
    @PostMapping
    public ResponseEntity<ConversationSession> createConversation(
            @RequestBody CreateConversationRequest request,
            @RequestHeader("X-User-Id") String userId) {
        
        ConversationSession session = conversationService.createSession(userId, request.getTitle());
        return ResponseEntity.ok(session);
    }
    
    @PostMapping("/{sessionId}/messages")
    public ResponseEntity<MessageResponse> sendMessage(
            @PathVariable String sessionId,
            @RequestBody MessageRequest request) {
        
        ConversationMessage message = conversationService.continueConversation(
            sessionId,
            request.getContent()
        );
        
        return ResponseEntity.ok(new MessageResponse(message));
    }
    
    @GetMapping("/{sessionId}/history")
    public ResponseEntity<List<ConversationMessage>> getHistory(@PathVariable String sessionId) {
        List<ConversationMessage> history = conversationService.getConversationHistory(sessionId);
        return ResponseEntity.ok(history);
    }
}
```

**数据库迁移脚本**:

```sql
-- V1__Create_conversation_tables.sql
CREATE TABLE conversation_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    message_count INT DEFAULT 0,
    total_tokens BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE conversation_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    token_count INT DEFAULT 0,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_session FOREIGN KEY (session_id) REFERENCES conversation_sessions(id)
);

CREATE INDEX idx_sessions_user ON conversation_sessions(user_id);
CREATE INDEX idx_messages_session ON conversation_messages(session_id);
```

---

### Task 3.2: 智能内存压缩
**优先级**: 🟡 中 | **依赖**: Task 3.1 | **预计时间**: 3小时

**描述**: 实现Token管理和历史记录压缩策略

**验收标准**:
- [ ] 准确估算Token使用量
- [ ] 自动压缩旧记录
- [ ] 支持多种压缩策略
- [ ] 不丢失重要信息
- [ ] 性能监控

**实现步骤**:

```java
// service/TokenManagementService.java
@Service
@Slf4j
@RequiredArgsConstructor
public class TokenManagementService {
    
    private static final int MAX_CONTEXT_TOKENS = 4000;
    private static final int TARGET_COMPRESSION_TOKENS = 2000;
    
    private final ConversationMessageRepository messageRepository;
    private final ChatService chatService;
    
    /**
     * 构建优化的上下文，自动处理Token限制
     */
    public String buildOptimizedContext(String sessionId, int maxTokens) {
        List<ConversationMessage> allMessages = 
            messageRepository.findBySessionIdOrderByTimestampAsc(sessionId);
        
        int totalTokens = allMessages.stream()
            .mapToInt(ConversationMessage::getTokenCount)
            .sum();
        
        // 如果超过限制，进行压缩
        if (totalTokens > maxTokens) {
            return compressAndBuildContext(allMessages, maxTokens);
        }
        
        return buildContextFromMessages(allMessages);
    }
    
    /**
     * 压缩对话历史
     */
    private String compressAndBuildContext(
            List<ConversationMessage> messages,
            int maxTokens) {
        
        if (messages.isEmpty()) return "";
        
        // 策略：保留最近的消息，压缩早期消息
        int messageCount = messages.size();
        int splitPoint = messageCount / 2;
        
        // 早期消息进行摘要
        List<ConversationMessage> earlyMessages = messages.subList(0, splitPoint);
        String summary = summarizeMessages(earlyMessages);
        
        // 最近消息保留原样
        List<ConversationMessage> recentMessages = messages.subList(splitPoint, messageCount);
        
        StringBuilder context = new StringBuilder();
        context.append("[对话摘要]\n").append(summary).append("\n\n");
        context.append("[最近对话]\n");
        context.append(buildContextFromMessages(recentMessages));
        
        return context.toString();
    }
    
    /**
     * 生成对话摘要
     */
    private String summarizeMessages(List<ConversationMessage> messages) {
        String conversationText = messages.stream()
            .map(m -> m.getRole() + ": " + m.getContent())
            .collect(Collectors.joining("\n"));
        
        String summary = chatService.chatWithSystemPrompt(
            "将以下对话总结为3-5句话的要点",
            conversationText
        );
        
        log.info("Generated summary for {} messages", messages.size());
        return summary;
    }
    
    /**
     * 从消息列表构建上下文
     */
    private String buildContextFromMessages(List<ConversationMessage> messages) {
        return messages.stream()
            .map(m -> String.format("%s: %s", m.getRole(), m.getContent()))
            .collect(Collectors.joining("\n"));
    }
    
    /**
     * 估算Token数量（改进版本）
     */
    public int estimateTokens(String text) {
        // 更精确的估算
        // 对于英文：约1 token = 4个字符
        // 对于中文：约1 token = 2-3个字符
        
        int englishChars = (int) text.chars()
            .filter(c -> (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'))
            .count();
        
        int chineseChars = (int) text.chars()
            .filter(c -> c > 0x4E00 && c < 0x9FA5)
            .count();
        
        int otherChars = text.length() - englishChars - chineseChars;
        
        return (englishChars / 4) + (chineseChars / 2) + (otherChars / 4) + 1;
    }
}
```

```java
// service/CompressionStrategyService.java
@Service
@Slf4j
public class CompressionStrategyService {
    
    enum CompressionStrategy {
        SUMMARY,      // 对早期消息进行摘要
        SAMPLING,     // 采样保留消息
        SLIDING_WINDOW  // 滑动窗口保留最近N条
    }
    
    /**
     * 执行消息压缩
     */
    public List<ConversationMessage> compress(
            List<ConversationMessage> messages,
            CompressionStrategy strategy,
            int targetTokens) {
        
        return switch (strategy) {
            case SUMMARY -> summarizeEarlyMessages(messages, targetTokens);
            case SAMPLING -> samplingStrategy(messages, targetTokens);
            case SLIDING_WINDOW -> slidingWindowStrategy(messages, targetTokens);
        };
    }
    
    private List<ConversationMessage> summarizeEarlyMessages(
            List<ConversationMessage> messages,
            int targetTokens) {
        // 实现摘要策略
        return messages;
    }
    
    private List<ConversationMessage> samplingStrategy(
            List<ConversationMessage> messages,
            int targetTokens) {
        // 实现采样策略
        int step = messages.size() / 3;
        return messages.stream()
            .filter(m -> messages.indexOf(m) % step == 0)
            .collect(Collectors.toList());
    }
    
    private List<ConversationMessage> slidingWindowStrategy(
            List<ConversationMessage> messages,
            int targetTokens) {
        // 保留最后N条消息
        int windowSize = Math.max(5, targetTokens / 100);
        return messages.subList(
            Math.max(0, messages.size() - windowSize),
            messages.size()
        );
    }
}
```

---

## 🔍 第三阶段: RAG 系统实现

### Task 4.1: 向量数据库集成
**优先级**: 🔴 高 | **依赖**: Task 1.2 | **预计时间**: 4小时

**描述**: 集成向量存储用于RAG

**验收标准**:
- [ ] 向量数据库成功连接（PgVector或其他）
- [ ] 支持文档向量化和存储
- [ ] 支持相似度搜索
- [ ] 搜索性能满足要求（<1s）
- [ ] 支持元数据过滤

**实现步骤**:

```java
// config/VectorStoreConfig.java
@Configuration
public class VectorStoreConfig {
    
    @Bean
    public VectorStore vectorStore(JdbcOperationsVectorStoreOptions options) {
        return new JdbcVectorStore(new JdbcOperationsVectorStore(
            jdbcOperations,
            new OpenAiEmbeddingModel(embeddingModel),
            options
        ));
    }
    
    @Bean
    public EmbeddingModel embeddingModel(OpenAiApi openAiApi) {
        return new OpenAiEmbeddingModel(openAiApi);
    }
}
```

```java
// service/DocumentIndexingService.java
@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentIndexingService {
    
    private final VectorStore vectorStore;
    private final EmbeddingModel embeddingModel;
    
    /**
     * 索引PDF文档
     */
    public void indexPdfDocument(File pdfFile, String documentType) {
        try {
            PdfDocumentReader pdfReader = new PdfDocumentReader(pdfFile);
            List<Document> documents = pdfReader.get();
            
            // 添加元数据
            documents.forEach(doc -> {
                doc.getMetadata().put("source", pdfFile.getName());
                doc.getMetadata().put("type", documentType);
                doc.getMetadata().put("indexed_at", System.currentTimeMillis());
            });
            
            // 分块处理
            TokenTextSplitter splitter = new TokenTextSplitter();
            List<Document> splitDocs = splitter.apply(documents);
            
            // 向量化并存储
            vectorStore.add(splitDocs);
            
            log.info("Indexed {} documents from {}", splitDocs.size(), pdfFile.getName());
            
        } catch (Exception e) {
            log.error("Failed to index PDF: {}", pdfFile.getName(), e);
            throw new DocumentIndexingException("Failed to index document", e);
        }
    }
    
    /**
     * 索引文本文档
     */
    public void indexTextDocument(String filePath, String documentType) {
        try {
            String content = Files.readString(Paths.get(filePath));
            Document doc = new Document(content);
            doc.getMetadata().put("source", filePath);
            doc.getMetadata().put("type", documentType);
            
            TokenTextSplitter splitter = new TokenTextSplitter();
            List<Document> splitDocs = splitter.apply(List.of(doc));
            
            vectorStore.add(splitDocs);
            log.info("Indexed text document: {}", filePath);
            
        } catch (IOException e) {
            log.error("Failed to index text document: {}", filePath, e);
            throw new DocumentIndexingException("Failed to index document", e);
        }
    }
    
    /**
     * 批量索引目录中的文档
     */
    public void indexDirectory(File directory, String documentType) {
        File[] files = directory.listFiles();
        if (files == null) return;
        
        for (File file : files) {
            if (file.isFile()) {
                if (file.getName().endsWith(".pdf")) {
                    indexPdfDocument(file, documentType);
                } else if (file.getName().endsWith(".txt")) {
                    indexTextDocument(file.getPath(), documentType);
                }
            }
        }
    }
    
    /**
     * 删除文档
     */
    public void deleteDocument(String documentName) {
        // 实现文档删除逻辑
        log.info("Deleted document: {}", documentName);
    }
}
```

```java
// service/DocumentSearchService.java
@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentSearchService {
    
    private final VectorStore vectorStore;
    
    /**
     * 相似度搜索
     */
    public List<Document> search(String query, int topK) {
        return vectorStore.similaritySearch(
            SearchRequest.query(query)
                .withTopK(topK)
                .withSimilarityThreshold(0.75)
        );
    }
    
    /**
     * 带元数据过滤的搜索
     */
    public List<Document> searchWithFilter(String query, String documentType, int topK) {
        return vectorStore.similaritySearch(
            SearchRequest.query(query)
                .withTopK(topK)
                .withSimilarityThreshold(0.75)
                .withFilterExpression("type == '" + documentType + "'")
        );
    }
    
    /**
     * 混合搜索（向量+关键词）
     */
    public List<Document> hybridSearch(String query, int topK) {
        // 向量搜索
        List<Document> vectorResults = search(query, topK);
        
        // 关键词搜索（如果实现了）
        List<Document> keywordResults = keywordSearch(query, topK);
        
        // 合并和去重
        return mergeAndRank(vectorResults, keywordResults, topK);
    }
    
    private List<Document> keywordSearch(String query, int topK) {
        // 实现关键词搜索（可选）
        return new ArrayList<>();
    }
    
    private List<Document> mergeAndRank(
            List<Document> vectorResults,
            List<Document> keywordResults,
            int topK) {
        
        // 合并两个结果集并按相关性排序
        Set<String> seen = new HashSet<>();
        List<Document> merged = new ArrayList<>();
        
        vectorResults.forEach(doc -> {
            String id = doc.getMetadata().get("id").toString();
            if (!seen.contains(id)) {
                merged.add(doc);
                seen.add(id);
            }
        });
        
        keywordResults.forEach(doc -> {
            String id = doc.getMetadata().get("id").toString();
            if (!seen.contains(id)) {
                merged.add(doc);
                seen.add(id);
            }
        });
        
        return merged.stream().limit(topK).collect(Collectors.toList());
    }
}
```

**数据库初始化**:

```sql
-- 创建向量存储表（PgVector）
CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE vector_store (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_id VARCHAR(255),
    content TEXT NOT NULL,
    embedding vector(1536),
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX ON vector_store USING ivfflat (embedding vector_cosine_ops);
```

---

### Task 4.2: RAG 增强与生成
**优先级**: 🔴 高 | **依赖**: Task 4.1, Task 2.1 | **预计时间**: 4小时

**描述**: 实现RAG管道，增强LLM的知识

**验收标准**:
- [ ] 基础RAG流程完整
- [ ] 支持多查询RAG
- [ ] 添加自动引用
- [ ] RAG结果准确性验证
- [ ] 性能优化（缓存等）

**实现步骤**:

```java
// service/RAGService.java
@Service
@Slf4j
@RequiredArgsConstructor
public class RAGService {
    
    private final ChatService chatService;
    private final DocumentSearchService searchService;
    
    /**
     * 基础RAG
     */
    public String ragChat(String userQuery) {
        // 第1步：检索相关文档
        List<Document> documents = searchService.search(userQuery, 5);
        
        if (documents.isEmpty()) {
            return "抱歉，我在知识库中找不到相关信息。";
        }
        
        // 第2步：构建增强提示
        String context = buildContext(documents);
        
        // 第3步：生成响应
        String response = chatService.chatWithSystemPrompt(
            buildRAGSystemPrompt(),
            buildRAGUserPrompt(context, userQuery)
        );
        
        // 第4步：添加引用
        return addCitations(response, documents);
    }
    
    /**
     * 多查询RAG - 生成多个查询变体进行检索
     */
    public String multiQueryRAG(String userQuery) {
        // 生成查询变体
        List<String> queryVariants = generateQueryVariants(userQuery);
        
        Set<Document> allDocuments = new HashSet<>();
        
        // 对每个查询变体进行检索
        for (String variant : queryVariants) {
            List<Document> docs = searchService.search(variant, 3);
            allDocuments.addAll(docs);
        }
        
        if (allDocuments.isEmpty()) {
            return "抱歉，我找不到相关信息。";
        }
        
        // 构建响应
        String context = buildContext(new ArrayList<>(allDocuments));
        String response = chatService.chatWithSystemPrompt(
            buildRAGSystemPrompt(),
            buildRAGUserPrompt(context, userQuery)
        );
        
        return addCitations(response, new ArrayList<>(allDocuments));
    }
    
    /**
     * 迭代RAG - 评估响应后可能进行二次检索
     */
    public String iterativeRAG(String userQuery) {
        String firstResponse = ragChat(userQuery);
        
        // 评估响应完整性
        String evaluation = evaluateResponse(userQuery, firstResponse);
        
        if (needsRefinement(evaluation)) {
            // 基于评估改进查询
            String improvedQuery = improveQuery(userQuery, evaluation);
            return ragChat(improvedQuery);
        }
        
        return firstResponse;
    }
    
    private List<String> generateQueryVariants(String query) {
        String variantsJson = chatService.chatWithSystemPrompt(
            """
            为用户查询生成3个不同的表述变体。
            返回JSON格式: {"variants": ["variant1", "variant2", "variant3"]}
            """,
            "原始查询: " + query
        );
        
        // 解析JSON并提取变体
        return parseVariants(variantsJson);
    }
    
    private String buildRAGSystemPrompt() {
        return """
            你是一个知识助手。
            
            请遵循以下规则：
            1. 使用提供的背景信息回答问题
            2. 如果信息不在背景中，请明确说明
            3. 在回答中引用信息来源
            4. 保持回答简洁明了
            5. 如果有多个相似的答案，提供最相关的
            """;
    }
    
    private String buildRAGUserPrompt(String context, String userQuery) {
        return String.format("""
            背景信息：
            %s
            
            用户问题：
            %s
            
            请基于上述背景信息回答用户的问题。
            """, context, userQuery);
    }
    
    private String buildContext(List<Document> documents) {
        return documents.stream()
            .map(doc -> {
                String source = doc.getMetadata()
                    .getOrDefault("source", "未知").toString();
                return String.format("[来源: %s]\n%s", source, doc.getContent());
            })
            .collect(Collectors.joining("\n\n---\n\n"));
    }
    
    private String addCitations(String response, List<Document> documents) {
        StringBuilder citedResponse = new StringBuilder(response);
        citedResponse.append("\n\n## 参考资源:\n");
        
        for (int i = 0; i < Math.min(documents.size(), 5); i++) {
            String source = documents.get(i).getMetadata()
                .getOrDefault("source", "未知").toString();
            citedResponse.append(String.format("%d. %s\n", i + 1, source));
        }
        
        return citedResponse.toString();
    }
    
    private String evaluateResponse(String query, String response) {
        return chatService.chatWithSystemPrompt(
            "评估以下回答的完整性和准确性。如果需要补充或修改，请说明。",
            "查询: " + query + "\n回答: " + response
        );
    }
    
    private boolean needsRefinement(String evaluation) {
        return evaluation.toLowerCase().contains("需要") ||
               evaluation.toLowerCase().contains("补充") ||
               evaluation.toLowerCase().contains("不足");
    }
    
    private String improveQuery(String originalQuery, String evaluation) {
        return chatService.chatWithSystemPrompt(
            "基于评估，改进原始查询以获得更好的结果。只返回改进后的查询，不包含其他内容。",
            "原始查询: " + originalQuery + "\n评估: " + evaluation
        );
    }
    
    private List<String> parseVariants(String json) {
        // 实现JSON解析
        return new ArrayList<>();
    }
}
```

```java
// controller/RAGController.java
@RestController
@RequestMapping("/api/v1/rag")
@Slf4j
@RequiredArgsConstructor
public class RAGController {
    
    private final RAGService ragService;
    private final DocumentIndexingService indexingService;
    private final DocumentSearchService searchService;
    
    /**
     * RAG查询
     */
    @PostMapping("/query")
    public ResponseEntity<RAGResponse> ragQuery(@RequestBody RAGQueryRequest request) {
        String response = ragService.ragChat(request.getQuery());
        return ResponseEntity.ok(new RAGResponse(response));
    }
    
    /**
     * 多查询RAG
     */
    @PostMapping("/multi-query")
    public ResponseEntity<RAGResponse> multiQueryRAG(@RequestBody RAGQueryRequest request) {
        String response = ragService.multiQueryRAG(request.getQuery());
        return ResponseEntity.ok(new RAGResponse(response));
    }
    
    /**
     * 文档上传和索引
     */
    @PostMapping("/documents/upload")
    public ResponseEntity<UploadResponse> uploadDocument(
            @RequestParam MultipartFile file,
            @RequestParam String documentType) {
        
        try {
            File tempFile = File.createTempFile("upload_", "_" + file.getOriginalFilename());
            file.transferTo(tempFile);
            
            if (file.getOriginalFilename().endsWith(".pdf")) {
                indexingService.indexPdfDocument(tempFile, documentType);
            } else {
                indexingService.indexTextDocument(tempFile.getPath(), documentType);
            }
            
            return ResponseEntity.ok(new UploadResponse(
                "文档上传成功",
                file.getOriginalFilename()
            ));
        } catch (Exception e) {
            log.error("Document upload failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new UploadResponse("上传失败: " + e.getMessage(), null));
        }
    }
}
```

---

## 🤖 第四阶段: Agent 开发

### Task 5.1: 基础 Agent 实现
**优先级**: 🔴 高 | **依赖**: Task 2.1 | **预计时间**: 5小时

**描述**: 实现基础的AI Agent系统，支持工具调用

**验收标准**:
- [ ] Agent能够理解任务并自主规划
- [ ] 支持多种工具调用
- [ ] 正确处理工具返回结果
- [ ] 支持错误恢复
- [ ] 执行过程可追踪

**实现步骤**:

```java
// service/agent/AgentToolRegistry.java
@Component
@Slf4j
public class AgentToolRegistry {
    
    private final Map<String, AgentTool> tools = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void registerDefaultTools() {
        // 注册系统工具
        registerTool(new DateTimeToolImpl());
        registerTool(new CalculatorToolImpl());
        registerTool(new WebSearchToolImpl());
        registerTool(new DatabaseQueryToolImpl());
    }
    
    public void registerTool(AgentTool tool) {
        tools.put(tool.getName(), tool);
        log.info("Registered agent tool: {}", tool.getName());
    }
    
    public AgentTool getTool(String name) {
        return tools.get(name);
    }
    
    public List<String> getToolDescriptions() {
        return tools.values().stream()
            .map(tool -> String.format("- %s: %s", tool.getName(), tool.getDescription()))
            .collect(Collectors.toList());
    }
}

interface AgentTool {
    String getName();
    String getDescription();
    Object execute(Map<String, Object> params) throws Exception;
}
```

```java
// service/agent/BasicAgentService.java
@Service
@Slf4j
@RequiredArgsConstructor
public class BasicAgentService {
    
    private final ChatService chatService;
    private final AgentToolRegistry toolRegistry;
    
    /**
     * 执行Agent任务
     */
    public AgentResponse executeTask(String userTask) {
        AgentState state = new AgentState();
        state.setTask(userTask);
        state.setStatus(AgentStatus.RUNNING);
        state.setStartTime(LocalDateTime.now());
        
        try {
            String response = agentLoop(userTask, state);
            state.setStatus(AgentStatus.COMPLETED);
            state.setResult(response);
        } catch (Exception e) {
            log.error("Agent execution failed", e);
            state.setStatus(AgentStatus.FAILED);
            state.setErrorMessage(e.getMessage());
        }
        
        state.setEndTime(LocalDateTime.now());
        return new AgentResponse(state);
    }
    
    /**
     * Agent循环
     */
    private String agentLoop(String userTask, AgentState state) {
        int maxIterations = 10;
        int iteration = 0;
        
        while (iteration < maxIterations) {
            iteration++;
            
            // 第一步：Agent决策
            AgentDecision decision = makeDecision(userTask, state);
            
            log.info("Agent iteration {}: {}", iteration, decision.getAction());
            
            // 第二步：执行决策
            String actionResult = executeAction(decision, state);
            
            // 第三步：更新状态
            state.addThought(decision.getThought());
            state.addAction(decision.getAction());
            state.addObservation(actionResult);
            
            // 第四步：检查是否完成
            if (decision.getAction().equals(AgentAction.FINISH)) {
                return decision.getOutput();
            }
        }
        
        throw new AgentExecutionException("Agent未在迭代限制内完成任务");
    }
    
    /**
     * Agent做出决策
     */
    private AgentDecision makeDecision(String userTask, AgentState state) {
        String toolsDescription = String.join("\n", toolRegistry.getToolDescriptions());
        
        String prompt = buildAgentPrompt(userTask, state, toolsDescription);
        
        String response = chatService.chatWithSystemPrompt(
            buildAgentSystemPrompt(),
            prompt
        );
        
        return parseAgentResponse(response);
    }
    
    /**
     * 执行Agent的决策
     */
    private String executeAction(AgentDecision decision, AgentState state) {
        switch (decision.getAction()) {
            case TOOL_USE -> {
                return executeTool(decision.getToolName(), decision.getToolParams());
            }
            case REASONING -> {
                return decision.getThought();
            }
            case FINISH -> {
                return decision.getOutput();
            }
            default -> {
                return "未知动作";
            }
        }
    }
    
    /**
     * 执行工具
     */
    private String executeTool(String toolName, Map<String, Object> params) {
        AgentTool tool = toolRegistry.getTool(toolName);
        if (tool == null) {
            return "工具不存在: " + toolName;
        }
        
        try {
            Object result = tool.execute(params);
            return result != null ? result.toString() : "工具执行成功，无返回值";
        } catch (Exception e) {
            log.error("Tool execution failed: {}", toolName, e);
            return "工具执行失败: " + e.getMessage();
        }
    }
    
    private String buildAgentSystemPrompt() {
        return """
            你是一个智能任务执行助手（Agent）。
            
            你的工作方式：
            1. 思考（Thought）：分析任务并制定计划
            2. 行动（Action）：选择使用工具或完成任务
            3. 观察（Observation）：查看行动结果
            4. 完成（Finish）：当你有最终答案时返回
            
            使用以下格式：
            思考：[你的分析]
            行动：[TOOL_USE | REASONING | FINISH]
            工具：[工具名称（如果使用TOOL_USE）]
            参数：{...}
            最终答案：[你的答案（如果FINISH）]
            """;
    }
    
    private String buildAgentPrompt(String userTask, AgentState state, String toolsDescription) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("用户任务: ").append(userTask).append("\n\n");
        
        if (!state.getThoughts().isEmpty()) {
            prompt.append("之前的思考过程:\n");
            state.getThoughts().forEach(t -> prompt.append("- ").append(t).append("\n"));
            prompt.append("\n");
        }
        
        prompt.append("可用工具:\n").append(toolsDescription).append("\n\n");
        
        prompt.append("请继续执行任务。");
        
        return prompt.toString();
    }
    
    private AgentDecision parseAgentResponse(String response) {
        // 实现响应解析，提取思考、行动、工具等
        // 这是一个简化的实现
        AgentDecision decision = new AgentDecision();
        
        if (response.contains("工具:")) {
            decision.setAction(AgentAction.TOOL_USE);
            // 解析工具名称和参数...
        } else if (response.contains("最终答案:")) {
            decision.setAction(AgentAction.FINISH);
            // 解析最终答案...
        } else {
            decision.setAction(AgentAction.REASONING);
        }
        
        return decision;
    }
}

// Agent相关的数据类
@Data
class AgentState {
    private String task;
    private AgentStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String result;
    private String errorMessage;
    
    private List<String> thoughts = new ArrayList<>();
    private List<String> actions = new ArrayList<>();
    private List<String> observations = new ArrayList<>();
    
    public void addThought(String thought) {
        thoughts.add(thought);
    }
    
    public void addAction(String action) {
        actions.add(action);
    }
    
    public void addObservation(String observation) {
        observations.add(observation);
    }
}

enum AgentStatus {
    RUNNING, COMPLETED, FAILED, PAUSED
}

enum AgentAction {
    TOOL_USE, REASONING, FINISH
}

@Data
@AllArgsConstructor
class AgentDecision {
    private AgentAction action;
    private String thought;
    private String toolName;
    private Map<String, Object> toolParams;
    private String output;
}

@Data
@AllArgsConstructor
class AgentResponse {
    private AgentState state;
}
```

```java
// 具体工具实现
@Component
class DateTimeToolImpl implements AgentTool {
    @Override
    public String getName() {
        return "get_current_time";
    }
    
    @Override
    public String getDescription() {
        return "获取当前日期和时间";
    }
    
    @Override
    public Object execute(Map<String, Object> params) {
        return LocalDateTime.now();
    }
}

@Component
class CalculatorToolImpl implements AgentTool {
    @Override
    public String getName() {
        return "calculate";
    }
    
    @Override
    public String getDescription() {
        return "执行数学计算。参数：expression (表达式)";
    }
    
    @Override
    public Object execute(Map<String, Object> params) throws Exception {
        String expression = (String) params.get("expression");
        // 实现安全的表达式计算
        return evaluateMathExpression(expression);
    }
    
    private double evaluateMathExpression(String expr) {
        // 实现
        return 0;
    }
}
```

**Task 5.1 的继续...**
继续Task 5.2到5.4，以及Task 6.1到7.3...
由于长度限制，以下给出简要大纲：

```
Task 5.2: 多Agent协作 (4小时) 🔴
- 实现Agent之间的通信
- 支持任务分配和汇总
- 处理Agent冲突

Task 5.3: Agent Skills系统 (3小时) 🟡
- 实现可组合的技能
- 动态技能加载
- 技能版本管理

Task 6.1: MCP客户端集成 (4小时) 🔴
- 实现MCP协议客户端
- 支持外部服务调用
- 错误处理和重试

Task 6.2: MCP服务器实现 (3小时) 🟡
- 实现MCP服务器
- 定义接口契约
- 性能优化

Task 7.1: 安全实现 (4小时) 🔴
- API密钥管理
- 内容过滤
- 用户认证授权

Task 7.2: 可观测性 (3小时) 🟡
- 分布式追踪
- 指标收集
- 日志聚合

Task 7.3: 生产部署 (5小时) 🔴
- Docker容器化
- Kubernetes部署
- CI/CD流程
```

---

## ✅ 整体进度跟踪

| 阶段 | 任务数 | 预计周期 | 优先级 | 状态 |
|------|--------|---------|--------|------|
| **初始化** | 2 | 1周 | 🔴🔴 | ⬜ |
| **基础功能** | 3 | 1.5周 | 🔴🟡🟡 | ⬜ |
| **对话管理** | 2 | 1周 | 🔴🟡 | ⬜ |
| **RAG系统** | 2 | 2周 | 🔴🔴 | ⬜ |
| **Agent开发** | 3 | 2周 | 🔴🟡🟡 | ⬜ |
| **企业集成** | 2 | 1.5周 | 🔴🟡 | ⬜ |
| **安全&部署** | 3 | 2周 | 🔴🟡🔴 | ⬜ |

**总计**: 17个核心任务，约12周开发周期

---

## 🔗 关键依赖关系

```
Task 1.1 (项目初始化)
    ├─ Task 1.2 (环境配置)
    │   ├─ Task 2.1 (ChatClient基础)
    │   │   ├─ Task 2.2 (REST API)
    │   │   ├─ Task 2.3 (错误处理)
    │   │   └─ Task 3.1 (会话管理)
    │   │       └─ Task 3.2 (内存压缩)
    │   ├─ Task 4.1 (向量数据库)
    │   │   └─ Task 4.2 (RAG)
    │   └─ Task 5.1 (基础Agent)
    └─ Task 7.3 (生产部署)
```

---

## 📚 推荐学习资源

- Spring AI官方文档：https://docs.spring.io/spring-ai/reference/
- PostgreSQL向量扩展：https://github.com/pgvector/pgvector
- OpenAI API文档：https://platform.openai.com/docs/
- 企业部署最佳实践：https://12factor.net/

---

**此工作清单设计用于AI Coding Agent的自动化开发和验证，每个Task包含明确的验收标准和可执行的实现步骤。**
