# Spring AI 与 LLM 完整教程
## 从基础到生产级企业应用

**版本**: 1.0 | **最后更新**: 2026年5月 | **目标受众**: Java应用架构师、分析师、技术顾问

---

## 📚 目录

1. [教程概览](#教程概览)
2. [第一部分: Spring AI 基础](#第一部分-spring-ai-基础)
3. [第二部分: Prompt Engineering & Memory Management](#第二部分-prompt-engineering--memory-management)
4. [第三部分: 高级模式 - Advisors & Interceptors](#第三部分-高级模式---advisors--interceptors)
5. [第四部分: 检索增强生成(RAG)](#第四部分-检索增强生成rag)
6. [第五部分: AI Agent 开发](#第五部分-ai-agent-开发)
7. [第六部分: 企业级应用 - MCP 集成](#第六部分-企业级应用---mcp-集成)
8. [第七部分: 安全性与可观测性](#第七部分-安全性与可观测性)
9. [案例研究](#案例研究)

---

## 教程概览

### 学习目标

通过本教程，您将学会：

✅ 使用Spring AI构建生产级AI应用  
✅ 实现对话管理和上下文维护  
✅ 构建RAG系统增强LLM能力  
✅ 开发可扩展的AI Agent系统  
✅ 集成MCP进行标准化服务连接  
✅ 部署安全、可观测的AI应用  

### 核心特点

| 特性 | 优势 |
|------|------|
| **模块化设计** | 支持热插拔、独立测试 |
| **可扩展架构** | 易于添加新功能、支持多Agent协作 |
| **企业就绪** | 内置安全、监控、日志能力 |
| **Spring生态** | 充分利用Spring Boot、Security、Cloud等 |
| **LLM不可知** | 支持OpenAI、Claude、Bedrock等多个LLM提供商 |

### 学习路径

```
初级开发者
    ↓
Spring Boot基础 → ChatClient入门 → Prompt工程
    ↓
中级开发者
    ↓
Memory管理 → Function Calling → 简单Agent
    ↓
高级架构师
    ↓
RAG系统 → Multi-Agent编排 → 生产部署 → 安全与可观测性
```

---

## 第一部分: Spring AI 基础

### 1.1 环境准备

#### 依赖配置

```xml
<!-- Spring AI BOM -->
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

<!-- 核心依赖 -->
<dependencies>
    <!-- Spring AI 核心 -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-spring-boot-starter</artifactId>
    </dependency>
    
    <!-- LLM 提供商 (选择其一或多个) -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-anthropic-spring-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-bedrock-spring-boot-starter</artifactId>
    </dependency>
    
    <!-- 向量存储 (用于RAG) -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-pgvector-store-spring-boot-starter</artifactId>
    </dependency>
    
    <!-- 文档处理 -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-pdf-document-reader</artifactId>
    </dependency>
    
    <!-- 观测与监控 -->
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-tracing-bridge-otel</artifactId>
    </dependency>
</dependencies>
```

#### 应用配置 (application.yml)

```yaml
spring:
  ai:
    # OpenAI 配置
    openai:
      api-key: ${OPENAI_API_KEY}
      base-url: https://api.openai.com/v1
      
    # Anthropic Claude 配置
    anthropic:
      api-key: ${ANTHROPIC_API_KEY}
      
    # AWS Bedrock 配置 (用于企业)
    bedrock:
      region: us-east-1
      
  datasource:
    url: jdbc:postgresql://localhost:5432/rag_db
    username: postgres
    password: postgres
    
  jpa:
    hibernate:
      ddl-auto: update
```

### 1.2 ChatClient 入门

#### 基础使用

```java
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AIChatService {
    
    private final ChatClient chatClient;
    
    public AIChatService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }
    
    // 简单对话
    public String chat(String message) {
        return chatClient.prompt()
            .user(message)
            .call()
            .content();
    }
    
    // 带系统提示的对话
    public String chatWithSystemPrompt(String systemPrompt, String userMessage) {
        return chatClient.prompt()
            .system(systemPrompt)
            .user(userMessage)
            .call()
            .content();
    }
    
    // 使用选项控制行为
    public String chatWithOptions(String message) {
        return chatClient.prompt()
            .user(message)
            .options(o -> o
                .temperature(0.7)
                .maxTokens(1000)
                .topP(0.9)
            )
            .call()
            .content();
    }
}
```

#### REST 控制器

```java
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    
    private final AIChatService aiService;
    
    @PostMapping("/simple")
    public ChatResponse simple(@RequestBody ChatRequest request) {
        return new ChatResponse(
            aiService.chat(request.getMessage()),
            request.getMessage()
        );
    }
    
    @PostMapping("/with-context")
    public ChatResponse withContext(@RequestBody ChatWithContextRequest request) {
        return new ChatResponse(
            aiService.chatWithSystemPrompt(
                request.getSystemPrompt(),
                request.getUserMessage()
            ),
            request.getUserMessage()
        );
    }
}
```

### 1.3 核心概念

#### ChatClient 架构

```
ChatClient
    ├── Prompt 构建
    │   ├── System Messages
    │   ├── User Messages
    │   └── Assistant Messages
    │
    ├── Call & Response
    │   ├── 同步调用
    │   ├── 流式调用
    │   └── 响应解析
    │
    └── 选项配置
        ├── Temperature
        ├── MaxTokens
        ├── TopP
        └── Stop Sequences
```

#### 常用参数解释

| 参数 | 范围 | 说明 | 使用场景 |
|------|------|------|--------|
| **temperature** | 0.0-2.0 | 随机性，低=确定，高=创意 | 低:事实问答，高:创意生成 |
| **max_tokens** | 1-无限 | 最大响应长度 | 控制成本和响应长度 |
| **top_p** | 0.0-1.0 | 核心采样，只考虑概率最高的token | 与temperature配合使用 |
| **presence_penalty** | -2.0-2.0 | 提高新词出现概率 | 增加多样性 |
| **frequency_penalty** | -2.0-2.0 | 降低重复词概率 | 减少重复 |

---

## 第二部分: Prompt Engineering & Memory Management

### 2.1 Prompt Engineering 最佳实践

#### 角色定义 Pattern

```java
public class RoleBasedPromptService {
    
    private final ChatClient chatClient;
    
    // 客户服务代理
    public String customerServiceAgent(String customerQuery) {
        return chatClient.prompt()
            .system("""
                你是一个专业的客户服务代理。
                
                指导原则:
                1. 保持礼貌和尊重
                2. 准确理解客户需求
                3. 提供快速有效的解决方案
                4. 如不确定，请直诚认可并提供替代方案
                
                可用操作:
                - 查询订单状态
                - 处理退货
                - 升级问题
                - 提供产品建议
                """)
            .user(customerQuery)
            .call()
            .content();
    }
    
    // 数据分析师
    public String dataAnalystPrompt(String analysisTask) {
        return chatClient.prompt()
            .system("""
                你是一个资深数据分析师。
                
                分析流程:
                1. 数据质量评估
                2. 异常值检测
                3. 趋势分析
                4. 统计验证
                5. 业务洞察
                
                输出要求:
                - 使用Markdown表格呈现数据
                - 包含置信度水平
                - 提供可行建议
                """)
            .user(analysisTask)
            .call()
            .content();
    }
}
```

#### Few-Shot Learning Pattern

```java
@Service
public class FewShotPromptService {
    
    private final ChatClient chatClient;
    
    // 意图分类示例
    public String classifyIntentWithExamples(String userMessage) {
        return chatClient.prompt()
            .system("""
                将以下用户消息分类为意图。
                
                示例:
                用户: "我想购买一个笔记本"
                意图: PURCHASE
                
                用户: "我的订单什么时候到达?"
                意图: TRACKING
                
                用户: "如何退货?"
                意图: RETURN
                
                用户: "推荐一个好的办公椅"
                意图: RECOMMENDATION
                """)
            .user("消息: " + userMessage)
            .user("意图: ")
            .call()
            .content();
    }
}
```

#### Chain-of-Thought (思维链) Pattern

```java
@Service
public class ChainOfThoughtService {
    
    private final ChatClient chatClient;
    
    public String solveWithThinking(String problem) {
        return chatClient.prompt()
            .system("""
                解决问题时，请遵循以下步骤:
                
                1. 问题分解: 将问题分解为更小的子问题
                2. 分析: 逐个分析每个子问题
                3. 推理: 基于分析进行逻辑推理
                4. 综合: 综合各部分得出结论
                5. 验证: 验证结论的正确性
                
                请在回答时展示您的思考过程。
                """)
            .user(problem)
            .call()
            .content();
    }
}
```

### 2.2 会话内存管理

#### 简单的对话历史管理

```java
@Service
public class ConversationService {
    
    private final ChatClient chatClient;
    private final Map<String, List<Message>> conversations = new ConcurrentHashMap<>();
    
    // Message 类定义
    @Data
    @AllArgsConstructor
    public static class Message {
        private MessageRole role;  // USER, ASSISTANT, SYSTEM
        private String content;
        private long timestamp;
    }
    
    // 开启新对话
    public String startConversation(String sessionId, String initialMessage) {
        conversations.put(sessionId, new ArrayList<>());
        return continueConversation(sessionId, initialMessage);
    }
    
    // 继续对话
    public String continueConversation(String sessionId, String userMessage) {
        var messages = conversations.get(sessionId);
        if (messages == null) {
            throw new IllegalArgumentException("Session not found: " + sessionId);
        }
        
        // 添加用户消息
        messages.add(new Message(MessageRole.USER, userMessage, System.currentTimeMillis()));
        
        // 构建提示词
        var prompt = chatClient.prompt();
        prompt.system("你是一个有帮助的AI助手。记住对话历史，提供一致的回应。");
        
        // 添加历史消息
        for (Message msg : messages) {
            if (msg.getRole() == MessageRole.USER) {
                prompt.user(msg.getContent());
            } else if (msg.getRole() == MessageRole.ASSISTANT) {
                prompt.assistant(msg.getContent());
            }
        }
        
        // 获取响应
        String response = prompt.call().content();
        
        // 存储助手回应
        messages.add(new Message(MessageRole.ASSISTANT, response, System.currentTimeMillis()));
        
        return response;
    }
    
    // 获取对话历史
    public List<Message> getConversationHistory(String sessionId) {
        return conversations.getOrDefault(sessionId, new ArrayList<>());
    }
    
    // 清除对话
    public void clearConversation(String sessionId) {
        conversations.remove(sessionId);
    }
}
```

#### 带Token管理的智能内存

```java
@Service
public class SmartMemoryService {
    
    private final ChatClient chatClient;
    private final OpenAiTokenCounterUtil tokenCounter;
    private static final int MAX_CONTEXT_TOKENS = 4000;
    
    public String chatWithSmartMemory(String sessionId, String userMessage) {
        var history = getOrCreateHistory(sessionId);
        
        // 添加新消息
        history.add(new ConversationMessage(MessageRole.USER, userMessage));
        
        // 计算token并压缩历史
        var compressedHistory = compressHistory(history);
        
        // 构建提示词
        var prompt = chatClient.prompt()
            .system("你是一个记忆深刻的AI助手。");
        
        for (ConversationMessage msg : compressedHistory) {
            if (msg.getRole() == MessageRole.USER) {
                prompt.user(msg.getContent());
            } else {
                prompt.assistant(msg.getContent());
            }
        }
        
        String response = prompt.call().content();
        history.add(new ConversationMessage(MessageRole.ASSISTANT, response));
        
        return response;
    }
    
    private List<ConversationMessage> compressHistory(List<ConversationMessage> history) {
        int tokenCount = 0;
        List<ConversationMessage> compressed = new ArrayList<>();
        
        // 从最近的消息开始往回添加
        for (int i = history.size() - 1; i >= 0; i--) {
            ConversationMessage msg = history.get(i);
            int msgTokens = tokenCounter.countTokens(msg.getContent());
            
            if (tokenCount + msgTokens > MAX_CONTEXT_TOKENS) {
                // 如果超过限制，为最早的消息添加摘要
                if (i > 0) {
                    String summary = createSummary(history.subList(0, i));
                    compressed.add(0, new ConversationMessage(
                        MessageRole.SYSTEM, 
                        "之前的对话摘要: " + summary
                    ));
                }
                break;
            }
            
            compressed.add(0, msg);
            tokenCount += msgTokens;
        }
        
        return compressed;
    }
    
    private String createSummary(List<ConversationMessage> messages) {
        var summaryPrompt = new StringBuilder(
            "将以下对话摘要为3-5句话的关键点:\n"
        );
        messages.forEach(m -> summaryPrompt.append(m.getRole())
            .append(": ")
            .append(m.getContent())
            .append("\n"));
        
        return summaryPrompt.toString();
    }
}
```

### 2.3 上下文管理最佳实践

```java
@Service
public class ContextManagementService {
    
    private final ChatClient chatClient;
    
    // 用户偏好上下文
    @Data
    public static class UserContext {
        private String userId;
        private String language;
        private String domain;
        private Map<String, String> preferences;
        private List<String> recentTopics;
    }
    
    public String chatWithUserContext(UserContext context, String message) {
        String contextPrompt = buildContextPrompt(context);
        
        return chatClient.prompt()
            .system(contextPrompt)
            .user(message)
            .call()
            .content();
    }
    
    private String buildContextPrompt(UserContext context) {
        return String.format("""
            用户背景信息:
            - 语言: %s
            - 领域: %s
            - 最近讨论的主题: %s
            - 用户偏好: %s
            
            请根据这些背景信息调整您的回应风格和内容深度。
            """,
            context.getLanguage(),
            context.getDomain(),
            String.join(", ", context.getRecentTopics()),
            context.getPreferences()
        );
    }
}
```

---

## 第三部分: 高级模式 - Advisors & Interceptors

### 3.1 Advisor 框架

#### 什么是 Advisor?

Advisor 是一个拦截器，用于在AI调用前后修改请求和响应。常见的Advisor包括：

- **ChatClientRequestAdvisor**: 修改请求
- **ChatClientResponseAdvisor**: 处理响应
- **GuardrailsAdvisor**: 内容安全检查
- **CachingAdvisor**: 响应缓存

#### 实现自定义 Advisor

```java
@Component
public class CustomAdvisors {
    
    // 1. 日志记录 Advisor
    public static ChatClientRequestAdvisor loggingAdvisor() {
        return (request) -> {
            log.info("ChatClient Request - Model: {}, Messages: {}",
                request.getModel(),
                request.getMessages().size());
            return request;
        };
    }
    
    // 2. 敏感信息检测 Advisor
    public static ChatClientRequestAdvisor sensitiveDataAdvisor() {
        return (request) -> {
            var messages = request.getMessages();
            messages.stream()
                .filter(m -> m.getContent() != null)
                .forEach(m -> {
                    // 检测并掩盖敏感信息
                    String masked = maskSensitiveData(m.getContent());
                    m.setContent(masked);
                });
            return request;
        };
    }
    
    // 3. 成本控制 Advisor
    public static ChatClientResponseAdvisor costAdvisor() {
        return (response) -> {
            var usage = response.getMetadata().getUsage();
            log.info("Token Usage - Input: {}, Output: {}, Total: {}",
                usage.getInputTokens(),
                usage.getOutputTokens(),
                usage.getInputTokens() + usage.getOutputTokens());
            return response;
        };
    }
    
    // 4. 内容验证 Advisor
    public static ChatClientResponseAdvisor validationAdvisor() {
        return (response) -> {
            String content = response.getResult().getOutput().getContent();
            
            // 验证内容质量
            if (!isValidResponse(content)) {
                // 触发重试或降级
                log.warn("Response validation failed");
            }
            
            return response;
        };
    }
    
    // 辅助方法
    private static String maskSensitiveData(String content) {
        // 掩盖信用卡号、社会安全号等
        return content
            .replaceAll("\\b\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}\\b", "****-****-****-****")
            .replaceAll("\\b\\d{3}-\\d{2}-\\d{4}\\b", "***-**-****");
    }
    
    private static boolean isValidResponse(String content) {
        return content != null && content.length() > 10;
    }
}
```

#### 在 ChatClient 中使用 Advisor

```java
@Service
public class AdvisedChatService {
    
    private final ChatClient chatClient;
    
    public String chatWithAdvisors(String message) {
        return chatClient.prompt()
            .user(message)
            // 添加多个 advisors
            .advisors(
                CustomAdvisors.loggingAdvisor(),
                CustomAdvisors.sensitiveDataAdvisor(),
                CustomAdvisors.validationAdvisor()
            )
            .call()
            .content();
    }
}
```

### 3.2 递归 Advisor 模式

```java
@Service
public class RecursiveAdvisorService {
    
    private final ChatClient chatClient;
    private static final int MAX_ITERATIONS = 3;
    
    // 自编辑内存 (Self-Editing Memory)
    public String chatWithSelfEditing(String message) {
        return chatClient.prompt()
            .user(message)
            .advisors(new ChatClientRequestAdvisor() {
                private int iteration = 0;
                
                @Override
                public ChatClientRequest apply(ChatClientRequest request) {
                    iteration++;
                    
                    if (iteration == 1) {
                        // 第一次: 生成初始响应
                        request.getMessages().add(new Message(
                            MessageRole.SYSTEM,
                            "生成一个初始响应，然后检查其质量。"
                        ));
                    } else if (iteration == 2) {
                        // 第二次: 自我反思
                        request.getMessages().add(new Message(
                            MessageRole.SYSTEM,
                            "审查您的响应。是否有改进空间？"
                        ));
                    }
                    
                    return request;
                }
            })
            .call()
            .content();
    }
    
    // 思维树 (Tree-of-Thought) 模式
    public String solveWithThoughtTree(String problem) {
        List<String> solutions = new ArrayList<>();
        
        // 生成多个思路
        for (int i = 0; i < 3; i++) {
            String approach = chatClient.prompt()
                .user("思路 " + (i + 1) + ": 如何解决这个问题: " + problem)
                .options(o -> o.temperature(0.8))
                .call()
                .content();
            solutions.add(approach);
        }
        
        // 评估和综合最佳方案
        String evaluation = chatClient.prompt()
                .system("你是一个问题解决评估专家。")
                .user("评估这些解决方案: " + solutions)
                .call()
                .content();
        
        return evaluation;
    }
}
```

---

## 第四部分: 检索增强生成(RAG)

### 4.1 RAG 核心概念

RAG (Retrieval-Augmented Generation) 通过以下步骤增强LLM能力：

```
1. 数据索引 (Data Indexing)
   ↓
2. 查询分析 (Query Analysis)
   ↓
3. 检索 (Retrieval)
   ↓
4. 增强 (Augmentation)
   ↓
5. 生成 (Generation)
   ↓
6. 输出 (Output with Citations)
```

### 4.2 文档索引与分块

```java
@Service
public class DocumentIndexingService {
    
    private final VectorStore vectorStore;
    private final EmbeddingModel embeddingModel;
    
    // 文档分块配置
    @Configuration
    public static class DocumentProcessingConfig {
        
        @Bean
        public TokenTextSplitter textSplitter() {
            return new TokenTextSplitter(512, 100, 5);  // chunkSize, minChunkSize, minOverlapSize
        }
        
        @Bean
        public DocumentReader documentReader() {
            return new DocumentReader();
        }
    }
    
    // 索引PDF文档
    public void indexPdfDocument(String filePath, String documentType) {
        // 加载文档
        var pdfReader = new PdfDocumentReader(new File(filePath));
        List<Document> documents = pdfReader.get();
        
        // 添加元数据
        documents.forEach(doc -> {
            doc.getMetadata().put("source", filePath);
            doc.getMetadata().put("type", documentType);
            doc.getMetadata().put("indexed_at", System.currentTimeMillis());
        });
        
        // 分块处理
        var splitter = new TokenTextSplitter();
        List<Document> splitDocs = splitter.apply(documents);
        
        // 存储到向量数据库
        vectorStore.add(splitDocs);
        
        log.info("Indexed {} documents from {}", splitDocs.size(), filePath);
    }
    
    // 索引多种格式文档
    public void indexMultiFormatDocuments(String documentPath) {
        if (documentPath.endsWith(".pdf")) {
            indexPdfDocument(documentPath, "PDF");
        } else if (documentPath.endsWith(".txt")) {
            indexTextDocument(documentPath, "TEXT");
        } else if (documentPath.endsWith(".md")) {
            indexMarkdownDocument(documentPath, "MARKDOWN");
        }
    }
    
    // 文本文档索引
    private void indexTextDocument(String filePath, String type) {
        try {
            String content = Files.readString(Paths.get(filePath));
            Document doc = new Document(content);
            doc.getMetadata().put("source", filePath);
            doc.getMetadata().put("type", type);
            
            var splitter = new TokenTextSplitter();
            vectorStore.add(splitter.apply(List.of(doc)));
        } catch (IOException e) {
            log.error("Error indexing document: {}", filePath, e);
        }
    }
    
    private void indexMarkdownDocument(String filePath, String type) {
        // 类似文本处理，可添加markdown特定逻辑
        indexTextDocument(filePath, type);
    }
}
```

### 4.3 高级检索策略

```java
@Service
public class AdvancedRetrievalService {
    
    private final VectorStore vectorStore;
    private final ChatClient chatClient;
    
    // 1. 基础向量检索
    public List<Document> vectorSearch(String query, int topK) {
        return vectorStore.similaritySearch(
            SearchRequest.query(query)
                .withTopK(topK)
                .withSimilarityThreshold(0.75)
        );
    }
    
    // 2. 混合检索 (向量 + 关键词)
    public List<Document> hybridSearch(String query, int topK) {
        // 向量检索
        var vectorResults = vectorSearch(query, topK);
        
        // 关键词检索 (需要集成全文搜索引擎)
        var keywordResults = keywordSearch(query, topK);
        
        // 合并和排序
        return mergeAndRankResults(vectorResults, keywordResults, topK);
    }
    
    // 3. 多步检索 (Multi-step Retrieval)
    public List<Document> multiStepRetrieval(String originalQuery) {
        // 第一步: 查询扩展
        String expandedQuery = expandQuery(originalQuery);
        
        // 第二步: 初始检索
        var initialResults = vectorSearch(expandedQuery, 10);
        
        // 第三步: 基于结果的细化检索
        String refinedQuery = refineQueryBasedOnResults(originalQuery, initialResults);
        
        // 第四步: 最终检索
        return vectorSearch(refinedQuery, 5);
    }
    
    // 4. 主题感知检索
    public List<Document> topicAwareRetrieval(String query, String topic) {
        return vectorStore.similaritySearch(
            SearchRequest.query(query)
                .withTopK(10)
                .withFilterExpression("type == '" + topic + "'")
        );
    }
    
    // 5. 时间感知检索
    public List<Document> timeAwareRetrieval(String query, LocalDateTime since) {
        long timestamp = since.atZone(ZoneId.systemDefault())
            .toInstant().toEpochMilli();
        
        return vectorStore.similaritySearch(
            SearchRequest.query(query)
                .withTopK(10)
                .withFilterExpression("indexed_at >= " + timestamp)
        );
    }
    
    // 辅助方法
    private String expandQuery(String query) {
        return chatClient.prompt()
            .system("扩展用户查询为5个相关的查询变体，用逗号分隔。")
            .user("原始查询: " + query)
            .call()
            .content();
    }
    
    private String refineQueryBasedOnResults(String originalQuery, List<Document> results) {
        String context = results.stream()
            .limit(3)
            .map(Document::getContent)
            .collect(Collectors.joining("\n"));
        
        return chatClient.prompt()
            .system("基于检索结果，改进原始查询以获得更相关的结果。")
            .user("原始查询: " + originalQuery)
            .user("初始结果上下文: " + context)
            .call()
            .content();
    }
    
    private List<Document> keywordSearch(String query, int topK) {
        // 使用 Elasticsearch、PostgreSQL FTS 等
        return new ArrayList<>();  // 实现留作练习
    }
    
    private List<Document> mergeAndRankResults(
            List<Document> vectorResults, 
            List<Document> keywordResults, 
            int topK) {
        // 实现结果合并和排序逻辑
        return vectorResults.stream()
            .limit(topK)
            .collect(Collectors.toList());
    }
}
```

### 4.4 RAG 增强与生成

```java
@Service
public class RAGGenerationService {
    
    private final ChatClient chatClient;
    private final AdvancedRetrievalService retrievalService;
    
    // 基础 RAG
    public String ragChat(String userQuery) {
        // 第1步: 检索相关文档
        List<Document> documents = retrievalService.vectorSearch(userQuery, 5);
        
        // 第2步: 构建增强提示
        String context = buildContext(documents);
        
        // 第3步: 生成响应
        String response = chatClient.prompt()
            .system("""
                你是一个知识助手。使用以下背景信息回答用户的问题。
                如果信息不在背景中，请明确说明。
                """)
            .user("背景信息:\n" + context)
            .user("问题: " + userQuery)
            .call()
            .content();
        
        // 第4步: 添加引用
        return addCitations(response, documents);
    }
    
    // 迭代 RAG (带反思)
    public String iterativeRAGChat(String userQuery) {
        String response = ragChat(userQuery);
        
        // 评估响应完整性
        String evaluation = chatClient.prompt()
            .system("评估回答的完整性。是否需要检索更多信息？")
            .user("用户问题: " + userQuery)
            .user("当前回答: " + response)
            .call()
            .content();
        
        // 如果不完整，进行第二轮检索
        if (evaluation.toLowerCase().contains("需要") || 
            evaluation.toLowerCase().contains("不够")) {
            
            String improvedQuery = chatClient.prompt()
                .system("基于评估，提出改进的搜索查询。")
                .user("原始问题: " + userQuery)
                .user("评估: " + evaluation)
                .call()
                .content();
            
            return ragChat(improvedQuery);
        }
        
        return response;
    }
    
    // 多查询 RAG
    public String multiQueryRAG(String userQuery) {
        // 生成多个查询变体
        var queries = chatClient.prompt()
            .system("""
                生成3个查询变体来全面回答用户问题。
                每个查询一行。
                """)
            .user("用户问题: " + userQuery)
            .call()
            .content()
            .split("\n");
        
        Set<Document> allDocuments = new HashSet<>();
        
        // 对每个查询进行检索
        for (String query : queries) {
            if (!query.trim().isEmpty()) {
                allDocuments.addAll(
                    retrievalService.vectorSearch(query.trim(), 3)
                );
            }
        }
        
        // 生成最终响应
        String context = buildContext(new ArrayList<>(allDocuments));
        return chatClient.prompt()
            .system("综合以下信息回答问题:")
            .user("背景:\n" + context)
            .user("问题: " + userQuery)
            .call()
            .content();
    }
    
    // 构建上下文
    private String buildContext(List<Document> documents) {
        return documents.stream()
            .map(doc -> {
                String source = doc.getMetadata().getOrDefault("source", "未知").toString();
                return String.format("[来源: %s]\n%s", source, doc.getContent());
            })
            .collect(Collectors.joining("\n\n---\n\n"));
    }
    
    // 添加引用
    private String addCitations(String response, List<Document> documents) {
        StringBuilder citedResponse = new StringBuilder(response);
        citedResponse.append("\n\n## 参考资源:\n");
        
        for (int i = 0; i < documents.size(); i++) {
            String source = documents.get(i).getMetadata()
                .getOrDefault("source", "未知").toString();
            citedResponse.append(i + 1).append(". ").append(source).append("\n");
        }
        
        return citedResponse.toString();
    }
}
```

### 4.5 RAG 实战案例: 企业知识库系统

```java
// 企业知识库 RAG 系统
@Service
@RequiredArgsConstructor
public class EnterpriseKnowledgeRAGService {
    
    private final DocumentIndexingService indexingService;
    private final RAGGenerationService ragService;
    private final VectorStore vectorStore;
    
    // 初始化知识库
    public void initializeKnowledgeBase(String knowledgeBasePath) {
        File folder = new File(knowledgeBasePath);
        for (File file : folder.listFiles()) {
            if (isValidDocument(file)) {
                indexingService.indexMultiFormatDocuments(file.getPath());
            }
        }
    }
    
    // 企业政策查询
    public String queryPolicy(String policyQuestion) {
        return ragService.ragChat("企业政策相关: " + policyQuestion);
    }
    
    // 技术文档查询
    public String queryTechnicalDoc(String techQuestion) {
        return ragService.ragChat("技术文档相关: " + techQuestion);
    }
    
    // 客户支持查询
    public String querySupportFAQ(String customerQuestion) {
        return ragService.multiQueryRAG("客户支持FAQ: " + customerQuestion);
    }
    
    private boolean isValidDocument(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".pdf") || name.endsWith(".txt") || 
               name.endsWith(".md") || name.endsWith(".docx");
    }
}
```

---

## 第五部分: AI Agent 开发

### 5.1 Agent 基础架构

```
Agent 系统架构:

┌─────────────────────────────────────────┐
│         User Interface                   │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│   Agent Orchestrator                    │
│  (任务分配、协调、结果整合)             │
└──────────────┬──────────────────────────┘
               │
      ┌────────┼────────┐
      │        │        │
┌─────▼─┐ ┌───▼───┐ ┌──▼────┐
│Agent 1│ │Agent 2│ │Agent 3│
└─────┬─┘ └───┬───┘ └──┬────┘
      │       │       │
      └───┬───┴───┬───┘
          │       │
      ┌───▼───┐ ┌─▼────┐
      │Tools  │ │Memory│
      └───────┘ └──────┘
```

### 5.2 基础 Agent 实现

```java
@Service
public class SimpleAgentService {
    
    private final ChatClient chatClient;
    
    // 定义 Agent 工具
    @Component
    public static class AgentTools {
        
        @Tool("获取当前日期和时间")
        public String getCurrentDateTime() {
            return LocalDateTime.now().toString();
        }
        
        @Tool("执行数学计算")
        public String calculate(
                @JsonProperty(required = true) String expression) {
            try {
                // 使用表达式计算库
                double result = evaluateExpression(expression);
                return "计算结果: " + result;
            } catch (Exception e) {
                return "计算失败: " + e.getMessage();
            }
        }
        
        @Tool("查询天气信息")
        public String getWeather(
                @JsonProperty(required = true) String location) {
            // 调用真实天气API
            return "城市: " + location + " 天气: 晴朗，温度: 25°C";
        }
        
        @Tool("搜索信息")
        public String searchInformation(
                @JsonProperty(required = true) String query) {
            // 调用搜索引擎
            return "搜索 '" + query + "' 的结果...";
        }
        
        private double evaluateExpression(String expr) {
            // 实现表达式计算
            return 0;
        }
    }
    
    // 简单 Agent
    public String runSimpleAgent(String userTask) {
        return chatClient.prompt()
            .system("""
                你是一个任务执行助手。
                你可以访问以下工具来完成任务:
                - getCurrentDateTime: 获取当前时间
                - calculate: 执行计算
                - getWeather: 查询天气
                - searchInformation: 搜索信息
                
                请根据用户请求选择合适的工具来完成任务。
                """)
            .user(userTask)
            .call()
            .content();
    }
}
```

### 5.3 高级 Agent 模式

#### Agent Skills (代理技能)

```java
@Service
public class AgentSkillsService {
    
    private final ChatClient chatClient;
    
    // 技能注册系统
    @Data
    public static class Skill {
        private String id;
        private String name;
        private String description;
        private Function<Map<String, Object>, String> executor;
        private Map<String, String> parameters;
    }
    
    private Map<String, Skill> availableSkills = new ConcurrentHashMap<>();
    
    // 注册技能
    public void registerSkill(Skill skill) {
        availableSkills.put(skill.getId(), skill);
        log.info("Registered skill: {}", skill.getName());
    }
    
    // 定义常用技能
    @PostConstruct
    public void initializeSkills() {
        // 数据分析技能
        registerSkill(new Skill(
            "analyze-data",
            "数据分析",
            "分析提供的数据集，提供统计信息和洞察",
            params -> analyzeData(params),
            Map.of("data", "string", "analysis_type", "string")
        ));
        
        // 报告生成技能
        registerSkill(new Skill(
            "generate-report",
            "报告生成",
            "基于数据生成正式报告",
            params -> generateReport(params),
            Map.of("title", "string", "content", "string", "format", "string")
        ));
        
        // 决策建议技能
        registerSkill(new Skill(
            "provide-recommendation",
            "决策建议",
            "基于分析结果提供业务建议",
            params -> provideRecommendation(params),
            Map.of("context", "string", "constraints", "string")
        ));
    }
    
    // 执行Agent，动态调用技能
    public String executeWithSkills(String task) {
        String skillsDescription = buildSkillsDescription();
        
        String response = chatClient.prompt()
            .system("""
                你是一个智能任务执行助手。你可以使用以下技能完成任务:
                %s
                
                当需要使用技能时，使用格式: [SKILL: skill_id, PARAMS: {key: value}]
                """.formatted(skillsDescription))
            .user(task)
            .call()
            .content();
        
        // 解析并执行技能
        return executeSkillsInResponse(response);
    }
    
    private String executeSkillsInResponse(String response) {
        String result = response;
        Pattern pattern = Pattern.compile("\\[SKILL: (\\w+), PARAMS: ([^\\]]+)\\]");
        Matcher matcher = pattern.matcher(response);
        
        while (matcher.find()) {
            String skillId = matcher.group(1);
            String params = matcher.group(2);
            
            Skill skill = availableSkills.get(skillId);
            if (skill != null) {
                Map<String, Object> paramMap = parseParams(params);
                String skillResult = skill.getExecutor().apply(paramMap);
                result = result.replace(matcher.group(0), skillResult);
            }
        }
        
        return result;
    }
    
    private String buildSkillsDescription() {
        return availableSkills.values().stream()
            .map(s -> String.format("- %s: %s (参数: %s)",
                s.getId(), s.getName(), s.getDescription()))
            .collect(Collectors.joining("\n"));
    }
    
    // 技能实现
    private String analyzeData(Map<String, Object> params) {
        return "数据分析完成: 平均值=50, 方差=100";
    }
    
    private String generateReport(Map<String, Object> params) {
        return "报告已生成: " + params.get("title");
    }
    
    private String provideRecommendation(Map<String, Object> params) {
        return "建议: 基于数据，建议采取行动X";
    }
    
    private Map<String, Object> parseParams(String paramString) {
        // 实现JSON解析
        return new HashMap<>();
    }
}
```

#### AskUserQuestion 模式

```java
@Service
public class InteractiveAgentService {
    
    private final ChatClient chatClient;
    
    // Agent 在执行前询问用户
    public String executeWithUserClarification(String userRequest) {
        // 第一步: 分析用户需求，确定是否需要澄清
        String clarificationNeeded = chatClient.prompt()
            .system("分析用户请求，如果有歧义或缺少必要信息，列出需要澄清的问题。")
            .user(userRequest)
            .call()
            .content();
        
        if (clarificationNeeded.contains("问题:") || 
            clarificationNeeded.contains("需要:")) {
            
            // 向用户提问
            String userClarification = askUser(clarificationNeeded);
            
            // 基于澄清信息重新执行
            return chatClient.prompt()
                .system("基于用户的澄清信息，执行请求")
                .user("原始请求: " + userRequest)
                .user("澄清信息: " + userClarification)
                .call()
                .content();
        }
        
        // 直接执行
        return executeTask(userRequest);
    }
    
    private String askUser(String questions) {
        // 在实际应用中，这会触发UI交互
        log.info("需要用户澄清:\n{}", questions);
        return "用户回答: [模拟用户输入]";
    }
    
    private String executeTask(String task) {
        return chatClient.prompt()
            .system("执行用户任务")
            .user(task)
            .call()
            .content();
    }
}
```

#### Todo/Planning 模式

```java
@Service
public class PlanningAgentService {
    
    private final ChatClient chatClient;
    
    // 任务规划结构
    @Data
    public static class TaskPlan {
        private String goal;
        private List<Step> steps;
        private Map<String, String> dependencies;
        
        @Data
        public static class Step {
            private int order;
            private String description;
            private String requiredTools;
            private String expectedOutput;
        }
    }
    
    // 规划式Agent
    public String executeWithPlanning(String userGoal) {
        // 第一步: 生成计划
        TaskPlan plan = generatePlan(userGoal);
        
        log.info("执行计划:\n{}", plan);
        
        // 第二步: 逐步执行
        StringBuilder result = new StringBuilder();
        for (TaskPlan.Step step : plan.getSteps()) {
            String stepResult = executeStep(step);
            result.append("步骤 ").append(step.getOrder())
                .append(": ").append(stepResult).append("\n");
        }
        
        // 第三步: 综合结果
        return synthesizeResults(userGoal, result.toString());
    }
    
    private TaskPlan generatePlan(String goal) {
        String planJson = chatClient.prompt()
            .system("""
                为用户目标生成详细的执行计划。
                返回格式必须是有效的JSON:
                {
                  "goal": "目标描述",
                  "steps": [
                    {
                      "order": 1,
                      "description": "步骤描述",
                      "requiredTools": "所需工具",
                      "expectedOutput": "预期输出"
                    }
                  ]
                }
                """)
            .user("目标: " + goal)
            .call()
            .content();
        
        // 解析JSON到TaskPlan
        return parseJsonToPlan(planJson);
    }
    
    private String executeStep(TaskPlan.Step step) {
        return chatClient.prompt()
            .system("执行计划步骤")
            .user(step.getDescription())
            .call()
            .content();
    }
    
    private String synthesizeResults(String goal, String stepResults) {
        return chatClient.prompt()
            .system("综合所有步骤的结果，生成最终输出")
            .user("原始目标: " + goal)
            .user("步骤结果:\n" + stepResults)
            .call()
            .content();
    }
    
    private TaskPlan parseJsonToPlan(String json) {
        // 实现JSON解析
        return new TaskPlan();
    }
}
```

### 5.4 多Agent 协作

```java
@Service
public class MultiAgentOrchestrationService {
    
    private final ChatClient chatClient;
    
    // Agent 定义
    enum AgentRole {
        ANALYST("数据分析师"),
        RESEARCHER("研究员"),
        WRITER("内容作者"),
        REVIEWER("审核员");
        
        private String description;
        
        AgentRole(String description) {
            this.description = description;
        }
    }
    
    // 协调多个Agent完成复杂任务
    public String orchestrateAgents(String complexTask) {
        // 第一步: 任务分解
        Map<AgentRole, String> taskBreakdown = decomposeTask(complexTask);
        
        // 第二步: 并行执行
        Map<AgentRole, String> results = new ConcurrentHashMap<>();
        ExecutorService executor = Executors.newFixedThreadPool(4);
        
        for (var entry : taskBreakdown.entrySet()) {
            executor.submit(() -> {
                String result = runAgent(entry.getKey(), entry.getValue());
                results.put(entry.getKey(), result);
            });
        }
        
        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 第三步: 综合结果
        return synthesizeMultiAgentResults(complexTask, results);
    }
    
    private String runAgent(AgentRole role, String task) {
        return chatClient.prompt()
            .system(getAgentSystemPrompt(role))
            .user(task)
            .call()
            .content();
    }
    
    private String getAgentSystemPrompt(AgentRole role) {
        return switch (role) {
            case ANALYST -> """
                你是一个数据分析师。
                - 提供量化分析
                - 基于数据做出结论
                - 识别趋势和异常
                """;
            case RESEARCHER -> """
                你是一个研究员。
                - 进行深入研究
                - 收集多个观点
                - 验证信息
                """;
            case WRITER -> """
                你是一个内容作者。
                - 生成清晰、引人入胜的文本
                - 组织信息逻辑
                - 适应目标受众
                """;
            case REVIEWER -> """
                你是一个审核员。
                - 检查准确性
                - 验证逻辑
                - 确保质量标准
                """;
        };
    }
    
    private Map<AgentRole, String> decomposeTask(String task) {
        String breakdown = chatClient.prompt()
            .system("""
                将复杂任务分解为子任务。
                为每个子任务分配最适合的Agent角色。
                """)
            .user(task)
            .call()
            .content();
        
        // 解析分解结果并映射到Agent角色
        return parseTaskBreakdown(breakdown);
    }
    
    private String synthesizeMultiAgentResults(
            String originalTask, 
            Map<AgentRole, String> results) {
        
        StringBuilder combined = new StringBuilder();
        combined.append("多Agent协作结果:\n\n");
        
        for (var entry : results.entrySet()) {
            combined.append("【").append(entry.getKey().description).append("】\n")
                .append(entry.getValue()).append("\n\n");
        }
        
        return chatClient.prompt()
            .system("综合所有Agent的输出，生成最终、一致的结果")
            .user("原始任务: " + originalTask)
            .user(combined.toString())
            .call()
            .content();
    }
    
    private Map<AgentRole, String> parseTaskBreakdown(String breakdown) {
        return new HashMap<>();  // 实现留作练习
    }
}
```

---

## 第六部分: 企业级应用 - MCP 集成

### 6.1 MCP (Model Context Protocol) 概述

MCP 是一个标准化协议，用于AI模型与外部工具/服务的通信。

```
┌─────────────┐
│   Spring AI │
└──────┬──────┘
       │
    MCP 协议
       │
  ┌────┴────┬──────────┬──────────┐
  │          │          │          │
┌─▼──┐  ┌───▼──┐  ┌───▼──┐  ┌───▼──┐
│Database│ API  │ Search│Files  │
└──────┘ └──────┘ └──────┘ └──────┘
```

### 6.2 MCP 客户端实现

```java
@Service
@Configuration
public class MCPClientConfig {
    
    // MCP 客户端配置
    @Bean
    public MCPClientManager mcpClientManager() {
        MCPClientManager manager = new MCPClientManager();
        
        // 注册MCP服务
        manager.registerServer("database", "http://localhost:9001/mcp");
        manager.registerServer("search", "http://localhost:9002/mcp");
        manager.registerServer("files", "http://localhost:9003/mcp");
        
        return manager;
    }
}

// MCP 客户端使用
@Service
@RequiredArgsConstructor
public class MCPIntegrationService {
    
    private final MCPClientManager mcpManager;
    
    // 调用数据库服务
    public String queryDatabase(String sql) {
        MCPRequest request = MCPRequest.builder()
            .server("database")
            .method("execute_query")
            .params(Map.of("sql", sql))
            .build();
        
        MCPResponse response = mcpManager.call(request);
        return response.getResult().toString();
    }
    
    // 调用搜索服务
    public List<String> search(String query) {
        MCPRequest request = MCPRequest.builder()
            .server("search")
            .method("search")
            .params(Map.of("query", query, "limit", 10))
            .build();
        
        MCPResponse response = mcpManager.call(request);
        return (List<String>) response.getResult();
    }
    
    // 调用文件服务
    public String readFile(String path) {
        MCPRequest request = MCPRequest.builder()
            .server("files")
            .method("read_file")
            .params(Map.of("path", path))
            .build();
        
        MCPResponse response = mcpManager.call(request);
        return response.getResult().toString();
    }
}
```

### 6.3 AI Agent 集成 MCP

```java
@Service
public class MCPAwareAgentService {
    
    private final ChatClient chatClient;
    private final MCPIntegrationService mcpService;
    
    // Agent 可以通过 MCP 调用外部服务
    public String agentWithMCP(String userTask) {
        // 定义 MCP 工具
        String mcpToolsDescription = """
            可用的MCP服务:
            
            1. 数据库查询 (database_query)
               - 参数: sql (SQL查询语句)
               - 说明: 执行数据库查询
               
            2. 全文搜索 (full_text_search)
               - 参数: query (搜索词)
               - 说明: 在知识库中搜索
               
            3. 文件读取 (read_file)
               - 参数: path (文件路径)
               - 说明: 读取文件内容
            """;
        
        String response = chatClient.prompt()
            .system("""
                你是一个配备了企业工具的智能助手。
                你可以调用以下MCP服务来完成任务:
                %s
                
                当需要调用MCP服务时，使用格式:
                [MCP: service_name, PARAMS: {key: value}]
                """.formatted(mcpToolsDescription))
            .user(userTask)
            .call()
            .content();
        
        // 执行MCP调用
        return executeMCPCalls(response);
    }
    
    private String executeMCPCalls(String response) {
        String result = response;
        Pattern pattern = Pattern.compile("\\[MCP: (\\w+), PARAMS: ([^\\]]+)\\]");
        Matcher matcher = pattern.matcher(response);
        
        while (matcher.find()) {
            String serviceName = matcher.group(1);
            String params = matcher.group(2);
            
            String mcpResult = callMCPService(serviceName, params);
            result = result.replace(matcher.group(0), mcpResult);
        }
        
        return result;
    }
    
    private String callMCPService(String serviceName, String params) {
        try {
            return switch (serviceName) {
                case "database_query" -> mcpService.queryDatabase(extractParam(params, "sql"));
                case "full_text_search" -> mcpService.search(extractParam(params, "query")).toString();
                case "read_file" -> mcpService.readFile(extractParam(params, "path"));
                default -> "未知服务: " + serviceName;
            };
        } catch (Exception e) {
            return "MCP调用失败: " + e.getMessage();
        }
    }
    
    private String extractParam(String params, String key) {
        // 简单的参数提取
        Pattern pattern = Pattern.compile(key + "\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(params);
        return matcher.find() ? matcher.group(1) : "";
    }
}
```

### 6.4 MCP 服务器实现 (Java 端)

```java
// 实现一个 MCP 服务器
@RestController
@RequestMapping("/mcp")
public class CustomMCPServer {
    
    @PostMapping("/handle")
    public ResponseEntity<?> handleMCPRequest(@RequestBody MCPRequest request) {
        try {
            Object result = switch (request.getMethod()) {
                case "execute_query" -> executeQuery(request.getParams());
                case "search" -> performSearch(request.getParams());
                case "read_file" -> readFile(request.getParams());
                default -> throw new IllegalArgumentException("Unknown method");
            };
            
            return ResponseEntity.ok(new MCPResponse(result, null));
        } catch (Exception e) {
            return ResponseEntity.ok(new MCPResponse(null, e.getMessage()));
        }
    }
    
    private Object executeQuery(Map<String, Object> params) {
        String sql = (String) params.get("sql");
        // 执行数据库查询
        return "查询结果...";
    }
    
    private Object performSearch(Map<String, Object> params) {
        String query = (String) params.get("query");
        // 执行搜索
        return List.of("结果1", "结果2", "结果3");
    }
    
    private Object readFile(Map<String, Object> params) {
        String path = (String) params.get("path");
        // 读取文件
        return "文件内容...";
    }
}
```

---

## 第七部分: 安全性与可观测性

### 7.1 应用安全

```java
@Configuration
@EnableWebSecurity
public class AISecurityConfig {
    
    // 1. API 密钥管理
    @Bean
    public APIKeyManager apiKeyManager() {
        return new APIKeyManager();
    }
    
    // 2. 内容过滤
    @Component
    public class ContentFilterAdvisor implements ChatClientRequestAdvisor {
        
        @Override
        public ChatClientRequest apply(ChatClientRequest request) {
            // 检查和过滤有害内容
            var messages = request.getMessages();
            messages.forEach(msg -> {
                if (containsHarmfulContent(msg.getContent())) {
                    throw new SecurityException("检测到有害内容");
                }
            });
            return request;
        }
        
        private boolean containsHarmfulContent(String content) {
            // 实现内容检查逻辑
            return false;
        }
    }
    
    // 3. 用户认证和授权
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
            .antMatchers("/api/chat/**").authenticated()
            .antMatchers("/api/admin/**").hasRole("ADMIN")
            .anyRequest().permitAll()
            .and()
            .oauth2ResourceServer()
            .jwt();
        
        return http.build();
    }
    
    // 4. 速率限制
    @Component
    public class RateLimitAdvisor implements ChatClientRequestAdvisor {
        
        private final RateLimiter rateLimiter = RateLimiter.create(100); // 100 requests/second
        
        @Override
        public ChatClientRequest apply(ChatClientRequest request) {
            if (!rateLimiter.tryAcquire()) {
                throw new TooManyRequestsException("超过速率限制");
            }
            return request;
        }
    }
}
```

### 7.2 可观测性

```java
@Configuration
public class ObservabilityConfig {
    
    // 1. 分布式追踪
    @Bean
    public TracingAdvisor tracingAdvisor() {
        return new TracingAdvisor() {
            @Override
            public ChatClientRequest apply(ChatClientRequest request) {
                Span span = Tracer.currentTracer().startSpan("ai-chat-request");
                try (Tracer.SpanInScope ws = Tracer.currentTracer().withSpan(span)) {
                    span.tag("model", request.getModel());
                    span.tag("message_count", request.getMessages().size());
                    return request;
                }
            }
        };
    }
    
    // 2. 指标收集
    @Service
    public class MetricsService {
        
        private final MeterRegistry meterRegistry;
        
        public void recordAICall(String model, long duration, int tokens) {
            meterRegistry.timer("ai.call.duration")
                .record(Duration.ofMillis(duration));
            
            meterRegistry.counter("ai.tokens.used",
                "model", model)
                .increment(tokens);
            
            meterRegistry.gauge("ai.active.calls",
                AtomicInteger::get);
        }
    }
    
    // 3. 日志记录
    @Component
    public class LoggingAdvisor implements ChatClientRequestAdvisor {
        
        private static final Logger log = LoggerFactory.getLogger(LoggingAdvisor.class);
        
        @Override
        public ChatClientRequest apply(ChatClientRequest request) {
            MDC.put("chat_model", request.getModel());
            MDC.put("message_count", String.valueOf(request.getMessages().size()));
            
            log.info("ChatClient request started");
            
            return request;
        }
    }
}

// 日志输出示例
@Aspect
@Component
public class AILoggingAspect {
    
    private static final Logger log = LoggerFactory.getLogger(AILoggingAspect.class);
    
    @AfterReturning(pointcut = "@annotation(AIOperation)", returning = "result")
    public void logAIOperation(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();
        
        log.info("AI Operation: {} completed successfully", methodName);
        log.debug("Result: {}", result);
    }
    
    @AfterThrowing(pointcut = "@annotation(AIOperation)", throwing = "exception")
    public void logAIOperationError(JoinPoint joinPoint, Exception exception) {
        String methodName = joinPoint.getSignature().getName();
        
        log.error("AI Operation: {} failed with error: {}", methodName, exception.getMessage());
        log.debug("Stack trace", exception);
    }
}
```

### 7.3 生产部署检查清单

```yaml
安全与合规:
  ✓ API密钥通过环境变量或密钥管理系统存储
  ✓ 敏感信息过滤和日志脱敏
  ✓ 用户身份认证和授权
  ✓ 数据加密（传输中和静止时）
  ✓ GDPR/隐私法规合规性检查
  ✓ 审计日志完整性

可靠性:
  ✓ 错误处理和重试机制
  ✓ 超时配置
  ✓ 断路器模式
  ✓ 负载均衡
  ✓ 灾难恢复计划

可观测性:
  ✓ 分布式追踪配置
  ✓ 关键指标监控
  ✓ 日志聚合
  ✓ 告警和通知
  ✓ 仪表板可视化

性能:
  ✓ 响应时间SLA定义
  ✓ Token使用优化
  ✓ 缓存策略
  ✓ 数据库连接池配置
  ✓ 异步处理

测试:
  ✓ 单元测试
  ✓ 集成测试
  ✓ 安全测试
  ✓ 负载测试
  ✓ UAT
```

---

## 案例研究

### 案例1: 智能客户支持系统 (银行行业)

#### 系统架构

```
┌─────────────────────────────────────────────────┐
│         客户支持前端                             │
│  (Web/Mobile/WeChat)                            │
└────────────────────┬────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────┐
│  支持Agent编排层                                 │
│  - 意图识别                                      │
│  - 路由分配                                      │
└────────────────────┬────────────────────────────┘
                     │
         ┌───────────┼───────────┐
         │           │           │
    ┌────▼──┐  ┌────▼──┐  ┌────▼──┐
    │FAQ   │  │订单   │  │产品   │
    │Agent │  │Agent  │  │Agent  │
    └────┬──┘  └───┬──┘  └───┬──┘
         │        │        │
    ┌────▼────────▼────────▼──┐
    │  RAG系统                 │
    │ (知识库、历史记录)       │
    └────┬─────────────────────┘
         │
    ┌────▼─────────────────────┐
    │  企业系统集成(MCP)        │
    │ - CRM系统                │
    │ - 订单系统                │
    │ - 知识库                  │
    └──────────────────────────┘
```

#### 核心代码

```java
@Service
@Slf4j
public class BankCustomerSupportService {
    
    private final ChatClient chatClient;
    private final RAGGenerationService ragService;
    private final MCPIntegrationService mcpService;
    
    // 支持流程
    public SupportResponse handleCustomerQuery(String customerId, String query) {
        // 1. 获取客户上下文
        CustomerContext context = getCustomerContext(customerId);
        
        // 2. 识别意图
        SupportIntent intent = identifyIntent(query);
        
        // 3. 路由到专业Agent
        String response = routeToSpecializedAgent(intent, query, context);
        
        // 4. 升级处理
        if (requiresEscalation(response)) {
            return escalateToHuman(customerId, query, response);
        }
        
        return new SupportResponse(response, intent);
    }
    
    private SupportIntent identifyIntent(String query) {
        return chatClient.prompt()
            .system("""
                识别客户支持查询的意图。
                可能的意图: FAQ, ORDER_TRACKING, COMPLAINT, PRODUCT_INQUIRY, ACCOUNT_ISSUE
                """)
            .user(query)
            .call()
            .content()
            // 解析为 SupportIntent
            .map(content -> SupportIntent.valueOf(content))
            .orElse(SupportIntent.FAQ);
    }
    
    private String routeToSpecializedAgent(
            SupportIntent intent, 
            String query, 
            CustomerContext context) {
        
        return switch (intent) {
            case ORDER_TRACKING -> handleOrderTracking(query, context);
            case COMPLAINT -> handleComplaint(query, context);
            case PRODUCT_INQUIRY -> handleProductInquiry(query);
            default -> handleFAQ(query);
        };
    }
    
    private String handleOrderTracking(String query, CustomerContext context) {
        // 使用MCP查询CRM系统
        String orderInfo = mcpService.queryDatabase(
            "SELECT * FROM orders WHERE customer_id = ?",
            context.getCustomerId()
        );
        
        // 使用RAG增强响应
        return ragService.ragChat(
            "根据订单信息 " + orderInfo + " 回答: " + query
        );
    }
    
    private String handleFAQ(String query) {
        return ragService.ragChat("FAQ相关: " + query);
    }
    
    private String handleProductInquiry(String query) {
        return ragService.multiQueryRAG("产品: " + query);
    }
    
    private String handleComplaint(String query, CustomerContext context) {
        String complaint = chatClient.prompt()
            .system("""
                处理客户投诉。
                步骤:
                1. 表示同情和理解
                2. 记录投诉细节
                3. 提供解决方案或升级
                """)
            .user(query)
            .call()
            .content();
        
        // 记录投诉
        logComplaint(context.getCustomerId(), query, complaint);
        
        return complaint;
    }
    
    private CustomerContext getCustomerContext(String customerId) {
        // 从系统获取客户信息
        return new CustomerContext(customerId, /* 更多信息 */);
    }
    
    private boolean requiresEscalation(String response) {
        return response.contains("升级") || response.contains("人工");
    }
    
    private SupportResponse escalateToHuman(
            String customerId, 
            String originalQuery, 
            String agentResponse) {
        
        // 创建人工工单
        return new SupportResponse("已转接到人工客服，工单号: " + generateTicketId());
    }
    
    private void logComplaint(String customerId, String complaint, String response) {
        // 记录投诉日志
        log.info("Complaint from customer {}: {} -> Response: {}", 
            customerId, complaint, response);
    }
}

// 数据类
@Data
class CustomerContext {
    private String customerId;
    private String accountStatus;
    private List<String> recentOrders;
    private String preferredLanguage;
}

@Data
class SupportResponse {
    private String message;
    private SupportIntent intent;
}

enum SupportIntent {
    FAQ, ORDER_TRACKING, COMPLAINT, PRODUCT_INQUIRY, ACCOUNT_ISSUE
}
```

---

### 案例2: 企业财务报表分析系统 (制造业)

```java
@Service
@Slf4j
public class FinancialAnalysisService {
    
    private final MultiAgentOrchestrationService agentService;
    private final RAGGenerationService ragService;
    private final AdvancedRetrievalService retrievalService;
    
    // 财务报表分析
    public FinancialAnalysisReport analyzeFinancialReport(File reportFile) {
        // 1. 索引报表
        indexFinancialDocument(reportFile);
        
        // 2. 多Agent分析
        String analysis = agentService.orchestrateAgents(
            "分析财务报表: " + reportFile.getName()
        );
        
        // 3. 提取关键指标
        List<KeyMetric> metrics = extractKeyMetrics(analysis);
        
        // 4. 生成建议
        String recommendations = generateRecommendations(metrics);
        
        return new FinancialAnalysisReport(analysis, metrics, recommendations);
    }
    
    private void indexFinancialDocument(File file) {
        // 使用DocumentIndexingService索引
        log.info("Indexing financial document: {}", file.getName());
    }
    
    private List<KeyMetric> extractKeyMetrics(String analysis) {
        String metricsJson = chatClient.prompt()
            .system("""
                从分析中提取关键财务指标。
                返回JSON格式: 
                {
                  "metrics": [
                    {"name": "指标名", "value": 数值, "unit": "单位"}
                  ]
                }
                """)
            .user(analysis)
            .call()
            .content();
        
        return parseMetrics(metricsJson);
    }
    
    private String generateRecommendations(List<KeyMetric> metrics) {
        // 基于关键指标生成建议
        return "建议...";
    }
}
```

---

## 总结与学习路径

### 核心要点回顾

| 概念 | 关键点 |
|------|--------|
| **ChatClient** | Spring AI的核心，用于与LLM通信 |
| **Prompt Engineering** | 设计高效提示词的艺术和科学 |
| **Memory Management** | 维护上下文，处理Token限制 |
| **Advisors** | 拦截和修改AI请求/响应 |
| **RAG** | 通过检索增强LLM的事实性 |
| **Agents** | 自主完成任务的AI系统 |
| **MCP** | 标准化AI与外部系统的集成 |
| **安全&可观测性** | 生产就绪的必要条件 |

### 推荐学习顺序

```
Week 1-2: Spring AI 基础
  └─ ChatClient 入门
  └─ 配置与环境
  └─ 简单对话实现

Week 3-4: Prompt 工程
  └─ Prompt 最佳实践
  └─ Few-shot 学习
  └─ 思维链

Week 5-6: 内存管理
  └─ 会话管理
  └─ Token 优化
  └─ 上下文维护

Week 7-8: Advisors
  └─ 自定义Advisor
  └─ 递归模式
  └─ 流程控制

Week 9-12: RAG 系统
  └─ 文档索引
  └─ 检索策略
  └─ RAG 模式

Week 13-16: AI Agents
  └─ Agent 基础
  └─ Skills 系统
  └─ 多Agent 编排

Week 17-18: MCP 集成
  └─ MCP 客户端
  └─ MCP 服务器
  └─ 企业集成

Week 19-20: 安全与可观测性
  └─ 安全措施
  └─ 监控配置
  └─ 生产部署
```

### 资源链接

- 官方文档: https://docs.spring.io/spring-ai/reference/
- GitHub示例: https://github.com/spring-projects/spring-ai-examples
- 社区论坛: https://github.com/spring-projects/spring-ai/discussions

---

**本教程由企业应用架构师团队编写，基于2025-2026年最新的Spring AI实践**
