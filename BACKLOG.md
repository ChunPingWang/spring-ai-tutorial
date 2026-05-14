# Spring AI 教程實作 — 開發進度與後續工作

> 最後更新：2026-05-15  
> 方法論：DDD + 六角形架構 + BDD + TDD + SOLID  
> 構建：Maven 多模組 / Java 21 / Spring Boot 3.4.1 / Spring AI 1.0.0

---

## 一、完成總覽

| Sprint | 對應原 Task | Bounded Context | 狀態 |
|---|---|---|---|
| S0 基石 | 1.1, 1.2 | (基礎設施) | ✅ |
| S1 Conversation MVP | 2.1, 2.2, 2.3 | Conversation | ✅ |
| S2 Conversation 進階 | 3.1, 3.2 | Conversation | ✅ |
| S3 KnowledgeRetrieval | 4.1, 4.2（純檢索） | KnowledgeRetrieval | ✅ |
| **S4 AgentOrchestration** | **5.1, 5.2, 5.3** | **AgentOrchestration** | ⬜ |
| **S5 EnterpriseIntegration (MCP)** | **6.1, 6.2** | **EnterpriseIntegration** | ⬜ |
| **S6 PlatformOps 生產化** | **7.1, 7.2, 7.3** | **PlatformOps** | ⬜ |

**測試現況（`mvn verify`）**：
- conversation-domain: 21 unit tests
- conversation-application: 5 unit tests
- conversation-adapter: 2 Testcontainers IT
- knowledge-retrieval-domain: 14 unit tests
- knowledge-retrieval-application: 2 unit tests
- bootstrap (BDD): 4 Cucumber scenarios
- architecture-tests: 4 ArchUnit rules
- **BUILD SUCCESS**

---

## 二、後續 Sprint 規劃

### S4 — BC AgentOrchestration

> 對應原 Task 5.1（基礎 Agent）、5.2（多 Agent 協作）、5.3（Agent Skills 系統）

**目標**：實作 AI Agent 能呼叫工具完成任務（Spring AI Function Calling / Tool Calling）。

**模組**（三個新 module，沿用前面 BC 的分層模式）：
```
agent-orchestration-domain/
agent-orchestration-application/
agent-orchestration-adapter/
```

**Domain 層**：
- `AgentTask` (Aggregate Root)：一次 agent 執行的完整生命週期
- `ToolInvocation` (Entity)：單次工具呼叫（name + arguments + result）
- `Skill` (VO)：工具的中繼資料（name、description、input schema）
- `AgentPlan` (VO)：agent 計畫的步驟序列
- `AgentStrategy` (domain service 介面)：決策邏輯抽象（ReAct、Plan-Execute）
- `SimpleReactStrategy` 預設實作

**Application 層**：
- `port/in/ExecuteAgentTaskUseCase` + `ExecuteAgentTaskCommand` / `Result`
- `port/out/ToolRegistryPort` — 列出可用工具
- `port/out/ToolExecutorPort` — 實際執行
- `port/out/LlmReasoningPort` — agent planning 用（**和 Conversation 的 `LlmGatewayPort` 分開以避免跨 BC 耦合**；同一個 OpenAI adapter 可以同時實作這兩個介面）
- `AgentOrchestrationApplicationService`

**Adapter 層**：
- `SpringAiToolCallingAdapter implements LlmReasoningPort`（用 Spring AI 1.0 的 `ToolCallback` API）
- 兩個 demo tool：`CurrentTimeTool`、`CalculatorTool`（純 Java，不依賴外部服務）
- `AgentController`：`POST /api/agents/run`

**BDD 場景**：
- 「用戶問『現在幾點』→ agent 識別需要 time tool → 呼叫 → 拿到結果 → 回覆人類友善時間」
- 「用戶問『125 × 37 是多少』→ agent 用 calculator tool 算」
- WireMock 兩階段對話 stub：
  - 第一輪 OpenAI 回 `tool_calls` 陣列
  - 第二輪我們把工具結果回傳給 OpenAI，OpenAI 回最終文字

**Task 5.3 多 Agent 協作**：依時間決定是否拆出 S4.5
- `MultiAgentOrchestrationStrategy`：把子任務分配給多個專業 agent
- 若 S4 時間充裕一起做；不夠則拉到下個 sprint

**潛在地雷**：
- WireMock 兩階段對話比 S1 單輪複雜，需依請求順序 stub 不同回應（用 `Scenario` API）
- Agent loop 可能無限循環 → 必須 `maxIterations` 限制
- Spring AI 1.0 的 `ToolCallback` API 對 prompt 結構有預期，客製化 system prompt 可能破 stub

---

### S5 — BC EnterpriseIntegration（MCP）

> 對應原 Task 6.1（MCP 客戶端集成）、6.2（MCP 服務器實現）

**目標**：把 Conversation/Agent 接到 Model Context Protocol 生態。

**模組**：
```
mcp-integration-domain/        ← 可能很薄
mcp-integration-application/
mcp-integration-adapter/
```

**Domain 層**：
- `McpEndpoint`、`McpResource`、`McpTool` (VO)
- `McpSession` (Aggregate？視協議模型而定)

**Application ports**：
- `port/in/RegisterMcpServerUseCase`、`ListMcpToolsUseCase`、`InvokeMcpToolUseCase`
- `port/out/McpClientPort`（連到外部 MCP server）
- `port/out/McpServerPort`（暴露本地能力為 MCP server）

**Adapter 層**：
- Spring AI MCP starter（檢查當下版本是否仍 preview）
- MCP server 範例：把 `Conversation` BC 的 ChatController 包裝為 MCP-compatible
- MCP client 範例：連到外部 filesystem MCP server 拿資源

**BDD**：
- 「Agent 透過 MCP 拿到本地文檔列表 → 讀取一份 → 回答用戶問題」
- 需要 stub MCP server 的 JSON-RPC 協議（stdio 比 HTTP 難 mock）

**潛在地雷**：
- MCP 規範變動較頻繁，Spring AI 對應的 starter 可能仍 preview
- JSON-RPC over stdio 在 BDD 環境難 mock；可考慮先做 MCP-over-SSE/HTTP 變體

---

### S6 — PlatformOps 生產化

> 對應原 Task 7.1（安全）、7.2（可觀測性）、7.3（部署）

#### S6a — Security (Task 7.1)
- Spring Security + OAuth2 Resource Server 或 API Key 認證
- 敏感資料加密（Spring Cloud Vault / Bouncy Castle）
- Rate limiting（Bucket4j）
- 輸入驗證（Bean Validation + OWASP Java Encoder）
- BDD：未授權呼叫回 401、超出 rate limit 回 429

#### S6b — Observability (Task 7.2)
- Spring Boot Actuator Prometheus endpoint（已配 base，補 metrics 維度）
- 自訂 metrics：`ai.chat.calls`、`ai.tokens.used`、`ai.cache.hit_rate`、`ai.retrieval.topk.size`
- 分散式追蹤：Micrometer Tracing + Zipkin/Tempo
- 結構化 JSON 日誌（Spring Boot 3.4 內建 `logging.structured.format.console=ecs`）

#### S6c — Deployment (Task 7.3)
- `Dockerfile`（multi-stage build，jlink 縮小映像）
- `docker-compose.yml`（本地開發：app + postgres+pgvector + redis）
- Kubernetes manifests：Deployment、Service、ConfigMap、Secret、Ingress、HPA
- Helm chart（選做）

**潛在地雷**：
- Security filter chain 順序錯會把 Actuator `/actuator/health` 也擋掉 → BDD 立刻紅燈
- Prometheus scrape 路徑與 Spring Boot 3.x 預設要對齊
- K8S Secret 不能讓 OpenAI key 進到 ConfigMap

---

## 三、跨 sprint 延後事項

| 項目 | 來源 | 優先級 | 描述 |
|---|---|---|---|
| RAG-into-Chat 跨 BC 整合 | S3 延後 | **中** | Conversation BC 在每次 chat 前自動呼叫 KnowledgeRetrieval 拿 top-K context 注入 prompt；展示 hex 跨 BC 對接 |
| LLM 摘要式 `MemoryCompactionPolicy` | S2 延後 | 低 | 目前只有 `SlidingWindowCompactionPolicy`；加 `SummaryCompactionPolicy` 把舊訊息壓縮為一條 SYSTEM 摘要 |
| jtokkit-backed `TokenEstimator` | S2 延後 | 低 | 取代 `SimpleTokenEstimator` 的「1 token ≈ 4 chars」粗估，改用 BPE |
| GitHub Actions CI workflow | — | **中** | `.github/workflows/maven-verify.yml`：每次 push 自動跑 `mvn verify` |
| `docker-compose.yml` | — | 中 | 本地開發環境（postgres+pgvector + redis） |
| Live OpenAI smoke test | — | 低 | `@Tag("live")` 測試，用 `.env` 的真實 key 跑，預設 skip |
| Embedding cache | — | 低 | 同樣 prompt 不重複叫 embedding API（cost 優化） |
| 多 LLM provider 支援 | — | 低 | Anthropic / Ollama 各自實作 `LlmGatewayPort`，用 strategy 切換 |
| OpenAI key rotation | **緊急** | **高** | 之前在對話框貼過 key，git history 雖然沒有但 chat history 已暴露 |

---

## 四、操作提醒

### ⚠️ 安全
- **OpenAI key 曾貼在 chat 訊息中**，請立刻到 https://platform.openai.com/api-keys 廢掉並重發。rotate 後改 `.env` 即可（gitignore 已擋）。
- `.env` 被 `.gitignore` 擋住，但聊天訊息歷史擋不住，rotate 是唯一保險。

### 環境需求
- JDK 21
- Maven 3.9+
- Docker（Testcontainers 用；新版 Docker daemon 需 API ≥ 1.40，parent POM 已強制 `api.version=1.43`）
- 跑 app（非測試）需要 Postgres + pgvector extension

### 常踩雷快查（已歸納成模式，免重複踩）
1. **BDD 跨 step 共用狀態**：用 `@Component @ScenarioScope` bean（`HttpContext`、`ScenarioStore`）
2. **WireMock stub Spring AI LLM 呼叫**：必須強制 `HttpClient.Version.HTTP_1_1`，否則 h2c 升級會 `RST_STREAM`
3. **JPA aggregate 重寫含 unique index**：`findById ifPresent { delete; flush }` 後再 insert，避免 orphanRemoval 與 INSERT 衝突
4. **多模組 Spring Boot**：`@EnableJpaRepositories(basePackages = "com.tutorial.springai")` + `@EntityScan(basePackages = "com.tutorial.springai")` 都要顯式擴大
5. **JPA 整合測試**：用 `@DataJpaTest` slice + `@AutoConfigureTestDatabase(replace = NONE)` 避免載 Spring AI auto-config（會強制要 api-key）
6. **同類別實作多介面**：只暴露一個 `@Bean`，Spring 自動把多個介面解析到同一 instance（不要 wrap 第二、第三個 bean）
7. **Spring AI vector store schema**：`initialize-schema=false` + Flyway 主控；避免雙重 init
8. **Testcontainers + pgvector**：image 用 `pgvector/pgvector:pg16` + `.asCompatibleSubstituteFor("postgres")`

---

## 五、開發指令參考

```bash
# 全測試（unit + IT + BDD + ArchUnit）
mvn verify

# 只跑特定 module
mvn -pl conversation-domain test
mvn -pl conversation-adapter -am verify    # IT 需 -am 帶上 transitive deps

# 跑 app（需要本地 Postgres 已起）
mvn -pl bootstrap spring-boot:run

# Git
git status
git diff --cached
git push origin main
```

### Sprint 工作節奏（已 internalized）
1. 寫紅燈 BDD scenario 或 unit test
2. 實作至綠燈
3. ArchUnit 規則不能紅
4. `mvn verify` 全綠
5. zh-TW commit message + Claude co-author trailer
6. push 到 `origin/main`（auto-push 規則）

---

## 六、決策紀錄（非顯然選擇）

| 決策 | sprint | 原因 |
|---|---|---|
| 五個 BC：Conversation / KnowledgeRetrieval / AgentOrchestration / EnterpriseIntegration / PlatformOps | S0 規劃 | 對應 tutorial 文1 的七大章節，core/supporting/generic 分類清楚 |
| 每個 BC 拆三個 module（domain / application / adapter） | S0 | 強化六角形邊界，ArchUnit 容易守 |
| `domain` 純 Java、`application` 純 Java POJOs、Spring annotation 只在 `adapter` | S1 | 最大可測試性、provider 中立、ArchUnit 規則簡單 |
| Application service 不用 `@Service`，由 adapter 的 `@Configuration @Bean` 配線 | S1 | 與上條一致 |
| BDD 用 WireMock，不 mock OpenAI Java client | S1 | 走真實 HTTP 路徑，能抓到 protocol 層 bug（果然抓到 RST_STREAM） |
| 持久化用真 Postgres + Testcontainers，不用 H2 | S2 | 避免 H2 / Postgres 差異隱藏 bug（如 pgvector 完全沒有 H2 對應） |
| Flyway 主控 schema，Spring AI 各 vector store `initialize-schema=false` | S3 | 明確 migration 歷史、避免雙重 init / drift |
| S3 不做 RAG-into-Chat 整合 | S3 | 單一 sprint 不跨 BC，保持邊界清楚；之後當作獨立工作 |
| 用 `SimpleTokenEstimator (~4 chars/token)` 而非 jtokkit | S2 | tutorial 重點是 token-aware compaction 流程，BPE 精度可後補 |
| `KnowledgeRetrieval` 用單一 `VectorStorePort`（embed+store 一起），不另開 `EmbeddingPort` | S3 | Spring AI VectorStore 內部已封裝 embedding，多開 port 反而是 fake hex purity |

---

## 七、Bounded Context 對接點（未來整合）

```
Conversation ───[uses LlmGatewayPort]───────► OpenAI
     │
     │ 未來 (S3 延後項目)
     ▼
KnowledgeRetrieval ──[uses VectorStorePort]──► pgvector + OpenAI embeddings
     ▲
     │ 未來 (S5)
     │
AgentOrchestration ──[uses LlmReasoningPort + ToolExecutorPort]──► OpenAI + tools
     │
     │ 未來 (S5)
     ▼
EnterpriseIntegration (MCP) ─[adapts]─► 外部 MCP servers
     │
     │ 全部包在
     ▼
PlatformOps (Security / Observability / Deployment) — cross-cutting
```

跨 BC 通訊**只能透過 application port**（in/out），絕不可 adapter-to-adapter、絕不可 domain-to-domain。
