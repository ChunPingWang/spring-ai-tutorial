# Spring AI 实战案例库与快速开始指南

---

## 🚀 5分钟快速开始

### 1. 初始化项目

```bash
# 使用Maven创建项目
mvn archetype:generate \
  -DgroupId=com.example \
  -DartifactId=spring-ai-quickstart \
  -DarchetypeArtifactId=maven-archetype-quickstart

cd spring-ai-quickstart

# 或直接克隆示例
git clone https://github.com/spring-projects/spring-ai-examples.git
```

### 2. 添加依赖

```xml
<!-- pom.xml -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.0</version>
</parent>

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
        <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
    </dependency>
</dependencies>
```

### 3. 配置

```yaml
# application.yml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      model: gpt-4

server:
  port: 8080
```

### 4. 创建服务

```java
// ChatService.java
@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatClient chatClient;
    
    public String chat(String message) {
        return chatClient.prompt()
            .user(message)
            .call()
            .content();
    }
}

// ChatController.java
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    
    @PostMapping
    public ResponseEntity<String> chat(@RequestBody String message) {
        return ResponseEntity.ok(chatService.chat(message));
    }
}
```

### 5. 启动应用

```bash
# 设置API密钥
export OPENAI_API_KEY=your_key_here

# 运行
mvn spring-boot:run

# 测试
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '"Hello, AI!"'
```

---

## 📦 项目结构参考

### 完整企业级项目结构

```
spring-ai-enterprise/
├── docs/
│   ├── architecture/
│   │   ├── 系统架构图.drawio
│   │   ├── 数据流设计.md
│   │   └── 部署架构.md
│   ├── guides/
│   │   ├── 开发指南.md
│   │   ├── 部署指南.md
│   │   └── 运维手册.md
│   └── api/
│       └── openapi.yaml
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/ai/
│   │   │       ├── AiApplication.java
│   │   │       │
│   │   │       ├── config/
│   │   │       │   ├── AIConfig.java           # AI配置
│   │   │       │   ├── DatabaseConfig.java      # 数据库配置
│   │   │       │   ├── VectorStoreConfig.java   # 向量存储配置
│   │   │       │   ├── SecurityConfig.java      # 安全配置
│   │   │       │   └── CacheConfig.java         # 缓存配置
│   │   │       │
│   │   │       ├── controller/
│   │   │       │   ├── ChatController.java      # 对话接口
│   │   │       │   ├── RAGController.java       # RAG接口
│   │   │       │   ├── AgentController.java     # Agent接口
│   │   │       │   └── HealthController.java    # 健康检查
│   │   │       │
│   │   │       ├── service/
│   │   │       │   ├── ChatService.java
│   │   │       │   ├── ConversationService.java
│   │   │       │   ├── RAGService.java
│   │   │       │   ├── DocumentIndexingService.java
│   │   │       │   ├── AgentService.java
│   │   │       │   ├── TokenManagementService.java
│   │   │       │   └── MetricsService.java
│   │   │       │
│   │   │       ├── repository/
│   │   │       │   ├── ConversationRepository.java
│   │   │       │   ├── DocumentRepository.java
│   │   │       │   └── UserRepository.java
│   │   │       │
│   │   │       ├── entity/
│   │   │       │   ├── ConversationSession.java
│   │   │       │   ├── ConversationMessage.java
│   │   │       │   ├── Document.java
│   │   │       │   └── User.java
│   │   │       │
│   │   │       ├── dto/
│   │   │       │   ├── ChatRequest.java
│   │   │       │   ├── ChatResponse.java
│   │   │       │   ├── RAGQueryRequest.java
│   │   │       │   ├── RAGQueryResponse.java
│   │   │       │   └── ErrorResponse.java
│   │   │       │
│   │   │       ├── exception/
│   │   │       │   ├── AIServiceException.java
│   │   │       │   ├── GlobalExceptionHandler.java
│   │   │       │   └── CustomExceptions.java
│   │   │       │
│   │   │       ├── aspect/
│   │   │       │   ├── LoggingAspect.java
│   │   │       │   ├── MetricsAspect.java
│   │   │       │   └── SecurityAspect.java
│   │   │       │
│   │   │       └── util/
│   │   │           ├── TokenCalculator.java
│   │   │           ├── PromptBuilder.java
│   │   │           └── ValidationUtil.java
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       ├── logback-spring.xml
│   │       └── db/
│   │           └── migration/
│   │               ├── V1__Initial_schema.sql
│   │               ├── V2__Add_conversation_tables.sql
│   │               └── V3__Add_rag_tables.sql
│   │
│   └── test/
│       ├── java/
│       │   └── com/example/ai/
│       │       ├── unit/
│       │       │   ├── ChatServiceTest.java
│       │       │   ├── RAGServiceTest.java
│       │       │   └── AgentServiceTest.java
│       │       │
│       │       ├── integration/
│       │       │   ├── ChatControllerTest.java
│       │       │   ├── RAGControllerTest.java
│       │       │   └── DatabaseTest.java
│       │       │
│       │       └── performance/
│       │           ├── LoadTest.java
│       │           └── MemoryProfileTest.java
│       │
│       └── resources/
│           ├── application-test.yml
│           └── test-data.sql
│
├── scripts/
│   ├── docker/
│   │   ├── Dockerfile
│   │   ├── docker-compose.yml
│   │   └── .dockerignore
│   │
│   ├── kubernetes/
│   │   ├── deployment.yaml
│   │   ├── service.yaml
│   │   ├── configmap.yaml
│   │   ├── secret.yaml
│   │   └── ingress.yaml
│   │
│   ├── database/
│   │   ├── init.sql
│   │   └── seed-data.sql
│   │
│   └── ci-cd/
│       ├── .github/
│       │   └── workflows/
│       │       ├── test.yml
│       │       ├── build.yml
│       │       └── deploy.yml
│       │
│       └── gitlab-ci.yml
│
├── pom.xml
├── mvnw
├── mvnw.cmd
├── Dockerfile
├── docker-compose.yml
├── README.md
├── CONTRIBUTING.md
└── LICENSE
```

---

## 💼 实战案例一：智能FAQ系统

### 项目概述
**场景**: 银行客户服务中心  
**功能**: 自动回答常见问题，复杂问题转人工  
**技术栈**: Spring AI + PostgreSQL + Redis

### 核心代码

```java
// FAQService.java
@Service
@Slf4j
@RequiredArgsConstructor
public class FAQService {
    private final ChatService chatService;
    private final DocumentIndexingService indexingService;
    private final RAGService ragService;
    private final CacheManager cacheManager;
    
    /**
     * 处理FAQ查询
     */
    public FAQResponse answerFAQ(String userId, String question) {
        // 1. 检查缓存
        String cached = getFromCache(question);
        if (cached != null) {
            return new FAQResponse(cached, true, "cached");
        }
        
        // 2. 使用RAG检索相关FAQ
        List<Document> relatedFAQs = ragService.searchFAQDatabase(question, 3);
        
        if (relatedFAQs.isEmpty()) {
            return new FAQResponse(
                "我没找到相关答案，已转接客服团队处理",
                false,
                "escalated"
            );
        }
        
        // 3. 生成答案
        String answer = ragService.ragChat(
            "根据以下FAQ信息回答用户的问题:\n" +
            relatedFAQs.stream()
                .map(Document::getContent)
                .collect(Collectors.joining("\n\n")) +
            "\n\n用户问题: " + question
        );
        
        // 4. 缓存结果
        cacheAnswer(question, answer);
        
        // 5. 记录日志
        logQuery(userId, question, answer);
        
        return new FAQResponse(answer, true, "generated");
    }
    
    /**
     * 初始化FAQ数据库
     */
    public void initializeFAQDatabase(File faqDocument) {
        indexingService.indexPdfDocument(faqDocument, "FAQ");
        log.info("FAQ database initialized");
    }
    
    private String getFromCache(String question) {
        Cache cache = cacheManager.getCache("faq_answers");
        if (cache != null) {
            Cache.ValueWrapper wrapper = cache.get(question);
            return wrapper != null ? (String) wrapper.get() : null;
        }
        return null;
    }
    
    private void cacheAnswer(String question, String answer) {
        Cache cache = cacheManager.getCache("faq_answers");
        if (cache != null) {
            cache.put(question, answer);
        }
    }
    
    private void logQuery(String userId, String question, String answer) {
        log.info("User: {}, Question: {}, Answer length: {}",
            userId, question, answer.length());
    }
}

// FAQController.java
@RestController
@RequestMapping("/api/faq")
@RequiredArgsConstructor
public class FAQController {
    private final FAQService faqService;
    
    @PostMapping("/ask")
    public ResponseEntity<FAQResponse> askFAQ(
            @RequestBody FAQQuestion question,
            @RequestHeader("X-User-Id") String userId) {
        
        FAQResponse response = faqService.answerFAQ(userId, question.getText());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/init")
    public ResponseEntity<String> initializeFAQ(
            @RequestParam MultipartFile file) throws IOException {
        
        File tempFile = File.createTempFile("faq_", ".pdf");
        file.transferTo(tempFile);
        faqService.initializeFAQDatabase(tempFile);
        
        return ResponseEntity.ok("FAQ database initialized");
    }
}

// 数据类
@Data
class FAQQuestion {
    private String text;
}

@Data
@AllArgsConstructor
class FAQResponse {
    private String answer;
    private boolean success;
    private String source;  // cached, generated, escalated
}
```

### 测试用例

```java
@SpringBootTest
class FAQServiceTest {
    
    @Autowired
    private FAQService faqService;
    
    @Test
    void testAnswerFAQ() {
        FAQResponse response = faqService.answerFAQ("user123", "如何重置密码?");
        
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertFalse(response.getAnswer().isEmpty());
    }
    
    @Test
    void testCaching() {
        String question = "如何激活卡片?";
        
        // 第一次调用
        FAQResponse response1 = faqService.answerFAQ("user123", question);
        
        // 第二次调用（应该从缓存获取）
        FAQResponse response2 = faqService.answerFAQ("user123", question);
        
        assertEquals("cached", response2.getSource());
    }
}
```

---

## 💼 实战案例二：企业财务报表分析系统

### 项目概述
**场景**: 制造业财务分析  
**功能**: 上传报表，多Agent协作分析，生成报告  
**技术栈**: Spring AI + PostgreSQL + Elasticsearch

### 核心代码

```java
// FinancialAnalysisService.java
@Service
@Slf4j
@RequiredArgsConstructor
public class FinancialAnalysisService {
    
    private final MultiAgentOrchestrationService agentService;
    private final RAGService ragService;
    private final DocumentIndexingService indexingService;
    
    /**
     * 分析财务报表
     */
    public FinancialAnalysisReport analyzeReport(File reportFile, String reportType) {
        // 1. 索引报表
        indexingService.indexPdfDocument(reportFile, reportType);
        
        // 2. 多Agent分析
        Map<String, String> analysisResults = agentService.orchestrateAgents(
            buildAnalysisTask(reportFile)
        );
        
        // 3. 提取关键指标
        List<FinancialMetric> metrics = extractMetrics(analysisResults.get("analyst"));
        
        // 4. 生成建议
        List<String> recommendations = generateRecommendations(metrics);
        
        // 5. 生成报告
        String report = generateReport(reportFile.getName(), metrics, recommendations);
        
        return new FinancialAnalysisReport(
            reportFile.getName(),
            metrics,
            recommendations,
            report,
            LocalDateTime.now()
        );
    }
    
    private String buildAnalysisTask(File file) {
        return "请详细分析文件: " + file.getName() + " 中的财务数据。" +
               "分析师：请识别关键财务指标。" +
               "审计员：请验证数据准确性。" +
               "顾问：请提供业务洞察。";
    }
    
    private List<FinancialMetric> extractMetrics(String analysisText) {
        // 调用LLM提取结构化指标
        String metricsJson = ragService.ragChat(
            "从以下分析中提取财务指标，返回JSON格式: " + analysisText
        );
        
        return parseMetricsFromJson(metricsJson);
    }
    
    private List<String> generateRecommendations(List<FinancialMetric> metrics) {
        StringBuilder context = new StringBuilder();
        metrics.forEach(m ->
            context.append(String.format("%s: %.2f\n", m.getName(), m.getValue()))
        );
        
        String recommendations = ragService.ragChat(
            "基于以下财务指标，生成3-5条业务建议:\n" + context.toString()
        );
        
        return parseRecommendations(recommendations);
    }
    
    private String generateReport(String reportName, 
                                  List<FinancialMetric> metrics,
                                  List<String> recommendations) {
        // 生成格式化的分析报告
        StringBuilder report = new StringBuilder();
        report.append("# 财务分析报告\n\n");
        report.append("## 关键指标\n");
        metrics.forEach(m ->
            report.append(String.format("- %s: %.2f %s\n", 
                m.getName(), m.getValue(), m.getUnit()))
        );
        report.append("\n## 建议\n");
        recommendations.forEach(r ->
            report.append("- ").append(r).append("\n")
        );
        
        return report.toString();
    }
}

// FinancialAnalysisController.java
@RestController
@RequestMapping("/api/financial")
@RequiredArgsConstructor
public class FinancialAnalysisController {
    
    private final FinancialAnalysisService analysisService;
    
    @PostMapping("/analyze")
    public ResponseEntity<FinancialAnalysisReport> analyzeReport(
            @RequestParam MultipartFile file,
            @RequestParam String reportType) throws IOException {
        
        File tempFile = File.createTempFile("financial_", "_" + file.getOriginalFilename());
        file.transferTo(tempFile);
        
        FinancialAnalysisReport report = analysisService.analyzeReport(tempFile, reportType);
        
        return ResponseEntity.ok(report);
    }
}

// 数据类
@Data
@AllArgsConstructor
class FinancialMetric {
    private String name;
    private double value;
    private String unit;
    private String interpretation;
}

@Data
@AllArgsConstructor
class FinancialAnalysisReport {
    private String reportName;
    private List<FinancialMetric> metrics;
    private List<String> recommendations;
    private String fullReport;
    private LocalDateTime generatedAt;
}
```

---

## 💼 实战案例三：智能客户支持系统

### 项目概述
**场景**: 零售电商平台  
**功能**: 多渠道支持、智能路由、自学习系统  
**技术栈**: Spring AI + MongoDB + RabbitMQ

### 核心架构

```java
// CustomerSupportOrchestrator.java
@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerSupportOrchestrator {
    
    private final ChatService chatService;
    private final RAGService ragService;
    private final IntentClassificationService intentService;
    private final SupportAgentRouter routerService;
    
    /**
     * 处理用户支持请求
     */
    public SupportResponse handleSupportRequest(SupportTicket ticket) {
        // 1. 分类意图
        SupportIntent intent = intentService.classify(ticket.getContent());
        
        // 2. 检查是否需要人工
        if (intent.requiresHuman()) {
            return routeToHuman(ticket);
        }
        
        // 3. 自动应答
        String response = generateResponse(intent, ticket);
        
        // 4. 满意度预测
        double satisfaction = predictSatisfaction(ticket, response);
        
        // 5. 学习反馈
        if (satisfaction < 0.7) {
            logForImprovement(ticket, response);
        }
        
        return new SupportResponse(
            response,
            intent,
            satisfaction,
            false
        );
    }
    
    private String generateResponse(SupportIntent intent, SupportTicket ticket) {
        return switch (intent) {
            case ORDER_TRACKING -> handleOrderTracking(ticket);
            case PRODUCT_INQUIRY -> handleProductInquiry(ticket);
            case COMPLAINT -> handleComplaint(ticket);
            case RETURN_REQUEST -> handleReturn(ticket);
            default -> handleGeneral(ticket);
        };
    }
    
    private String handleOrderTracking(SupportTicket ticket) {
        // 调用订单系统获取信息
        String orderInfo = getOrderInformation(ticket.getCustomerId());
        
        return ragService.ragChat(
            "根据订单信息回答客户: " + ticket.getContent() + 
            "\n订单状态: " + orderInfo
        );
    }
    
    private SupportResponse routeToHuman(SupportTicket ticket) {
        // 创建工单转接人工
        return new SupportResponse(
            "我已将您的问题转接给我们的客服团队，稍后会有专员与您联系。" +
            "工单号: " + ticket.getId(),
            SupportIntent.HUMAN_REQUIRED,
            0.5,
            true
        );
    }
    
    private double predictSatisfaction(SupportTicket ticket, String response) {
        // 使用ML模型预测满意度
        return 0.85;  // 简化
    }
    
    private void logForImprovement(SupportTicket ticket, String response) {
        log.warn("Low satisfaction predicted for ticket: {}", ticket.getId());
        // 记录到学习数据集
    }
}

// 枚举和数据类
enum SupportIntent {
    ORDER_TRACKING(false),
    PRODUCT_INQUIRY(false),
    COMPLAINT(true),
    RETURN_REQUEST(false),
    GENERAL(false),
    HUMAN_REQUIRED(true);
    
    private final boolean requiresHuman;
    
    SupportIntent(boolean requiresHuman) {
        this.requiresHuman = requiresHuman;
    }
    
    public boolean requiresHuman() {
        return requiresHuman;
    }
}

@Data
@AllArgsConstructor
class SupportResponse {
    private String message;
    private SupportIntent intent;
    private double predictedSatisfaction;
    private boolean escalated;
}
```

---

## 🎯 部署和运维

### Docker Compose配置

```yaml
version: '3.8'

services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      OPENAI_API_KEY: ${OPENAI_API_KEY}
      DATABASE_URL: jdbc:postgresql://postgres:5432/ai_db
      REDIS_HOST: redis
    depends_on:
      - postgres
      - redis
      - pgvector

  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: ai_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"

  pgvector:
    image: pgvector/pgvector:pg15
    depends_on:
      - postgres
    environment:
      POSTGRES_DB: ai_db

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

volumes:
  postgres_data:
```

### Kubernetes部署

```yaml
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: spring-ai-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: spring-ai
  template:
    metadata:
      labels:
        app: spring-ai
    spec:
      containers:
      - name: app
        image: spring-ai:latest
        ports:
        - containerPort: 8080
        env:
        - name: OPENAI_API_KEY
          valueFrom:
            secretKeyRef:
              name: api-keys
              key: openai-key
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "2Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
```

---

## 📊 监控和告警

### Prometheus指标

```yaml
# prometheus.yml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'spring-ai'
    static_configs:
      - targets: ['localhost:8080']
    metrics_path: '/actuator/prometheus'
```

### 关键指标

```java
@Configuration
public class MetricsConfig {
    
    @Bean
    public MeterBinder chatMetrics() {
        return registry -> {
            // API调用计数
            Counter.builder("ai.chat.calls")
                .tag("model", "gpt-4")
                .register(registry);
            
            // Token使用量
            DistributionSummary.builder("ai.tokens.used")
                .register(registry);
            
            // 延迟
            Timer.builder("ai.chat.latency")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
        };
    }
}
```

---

## 🧪 测试

### 集成测试

```java
@SpringBootTest
@AutoConfigureMockMvc
class IntegrationTests {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testChatEndpoint() throws Exception {
        mockMvc.perform(post("/api/chat")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"message\":\"Hello\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.response").exists());
    }
}
```

### 性能测试

```java
// JMeter脚本或使用Apache Bench
// ab -n 1000 -c 10 http://localhost:8080/api/chat
```

---

## 📈 持续改进

### 评估框架

```java
@Service
public class EvaluationFramework {
    
    /**
     * 评估RAG系统质量
     */
    public RAGMetrics evaluateRAG(List<TestCase> testCases) {
        double avgPrecision = 0;
        double avgRecall = 0;
        double avgNDCG = 0;
        
        for (TestCase test : testCases) {
            List<Document> retrieved = ragService.search(test.getQuery());
            avgPrecision += calculatePrecision(retrieved, test.getRelevantDocs());
            avgRecall += calculateRecall(retrieved, test.getRelevantDocs());
            avgNDCG += calculateNDCG(retrieved, test.getRelevantDocs());
        }
        
        return new RAGMetrics(
            avgPrecision / testCases.size(),
            avgRecall / testCases.size(),
            avgNDCG / testCases.size()
        );
    }
}
```

---

**所有代码示例都可在 GitHub 上找到: https://github.com/spring-projects/spring-ai-examples**
