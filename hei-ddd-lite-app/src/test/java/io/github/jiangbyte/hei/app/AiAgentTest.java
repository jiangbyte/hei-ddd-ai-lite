package io.github.jiangbyte.hei.app;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapperSupplier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;

/**
 * AI 基础能力冒烟测试（单文件，对接 OpenAI 或兼容端点）。
 *
 * <h2>怎么跑</h2>
 * <pre>
 *   # 需设置 OPENAI_API_KEY；可选 OPENAI_BASE_URL / OPENAI_CHAT_MODEL / OPENAI_EMBEDDING_MODEL
 *   export OPENAI_API_KEY=sk-...
 *   export JAVA_HOME=/path/to/jdk-21
 *   mvn -pl hei-ddd-lite-app -Dtest=AiAgentTest#test_call test
 *   # 或 IDE 中直接点方法旁的绿色三角
 * </pre>
 *
 * <h2>先改这些常量（按运行环境）</h2>
 * <ul>
 *   <li>{@link #API_KEY} / {@link #BASE_URL} / {@link #CHAT_MODEL} / {@link #EMBEDDING_MODEL}
 *       — OpenAI API Key、服务地址与模型名；所有对话与向量化都用它们</li>
 *   <li>{@link #MILVUS_HOST} / {@link #MILVUS_PORT} / {@link #MILVUS_USERNAME}
 *       / {@link #MILVUS_PASSWORD} / {@link #MILVUS_COLLECTION}
 *       — 仅 {@link #test_milvus()} 需要；Milvus 需已启动且开启鉴权</li>
 *   <li>{@link #MCP_FS_ROOT} / {@link #MCP_SSE_URL}
 *       — 仅 {@link #test_mcp()} 需要；stdio 用已 package 的 ai-mcp-demo JAR，
 *       SSE 地址上需已启动同工程 MCP Server</li>
 * </ul>
 *
 * <h2>用例一览</h2>
 * <ul>
 *   <li>{@link #test_call()} — ChatModel 非流式</li>
 *   <li>{@link #test_stream()} — ChatModel 流式</li>
 *   <li>{@link #test_memory()} — 同 conversationId 两轮记忆</li>
 *   <li>{@link #test_rag()} — 内存向量 + QuestionAnswerAdvisor</li>
 *   <li>{@link #test_milvus()} — MilvusVectorStore RAG</li>
 *   <li>{@link #test_mcp()} — MCP 工具挂到 ChatClient</li>
 * </ul>
 */
@Slf4j
class AiAgentTest {

    // ========================= 可调参数（OpenAI） =========================

    /** OpenAI API Key（优先读环境变量 OPENAI_API_KEY） */
    static final String API_KEY = envOr("OPENAI_API_KEY", "");
    /** OpenAI 或兼容端点根地址 */
    static final String BASE_URL = envOr("OPENAI_BASE_URL", "https://api.openai.com");
    /** 对话模型名 */
    static final String CHAT_MODEL = envOr("OPENAI_CHAT_MODEL", "gpt-4o-mini");
    /** 向量模型名；维度需与 {@link #EMBEDDING_DIM} 一致（text-embedding-3-small 默认 1536） */
    static final String EMBEDDING_MODEL = envOr("OPENAI_EMBEDDING_MODEL", "text-embedding-3-small");

    // ========================= 可调参数（Milvus，仅 test_milvus） =========================

    /** Milvus 主机 */
    static final String MILVUS_HOST = "127.0.0.1";
    /** Milvus 端口，默认 19530 */
    static final int MILVUS_PORT = 19530;
    /**
     * Milvus 用户名（鉴权必填；与基础设施约定一致）。
     */
    static final String MILVUS_USERNAME = "root";
    /**
     * Milvus 密码（鉴权必填；默认从官方 {@code Milvus} 改为 {@code infra123!}）。
     */
    static final String MILVUS_PASSWORD = "infra123!";
    /** 测试用集合名；不存在且 initializeSchema=true 时会自动建（维度变更后勿复用旧集合） */
    static final String MILVUS_COLLECTION = "hei_ai_agent_test_openai";
    /** 向量维度，需与 EMBEDDING_MODEL 输出一致 */
    static final int EMBEDDING_DIM = 1536;

    // ========================= 可调参数（MCP，仅 test_mcp） =========================

    /** filesystem MCP 允许访问的根目录（传给 ai-mcp-demo --ai.mcp.filesystem.roots） */
    static final String MCP_FS_ROOT = "/tmp/ai-mcp-demo-sandbox";
    /** SSE MCP 服务 baseUrl，例如 http://127.0.0.1:8101 */
    static final String MCP_SSE_URL = "http://127.0.0.1:8101";
    /** MCP 会话超时（stdio 冷启动 + 模型工具调用） */
    static final Duration MCP_TIMEOUT = Duration.ofSeconds(120);

    // ========================= 固定演示数据（一般不用改） =========================

    /** 记忆窗口：最多保留多少条消息 */
    static final int MEMORY_MAX_MESSAGES = 20;
    /** RAG 检索条数 topK */
    static final int RAG_TOP_K = 3;
    /** 写入向量库的演示文档正文（test_rag 会问「张三几岁」） */
    static final String DEMO_DOC_TEXT = "张三今年 28 岁，做 Java 与 Spring AI。";

    private ChatModel chatModel;
    private EmbeddingModel embeddingModel;
    private VectorStore vectorStore;
    private ChatClient chatClient;

    /**
     * 每个用例前准备：ChatModel、EmbeddingModel、内存向量库、带记忆+RAG 的 ChatClient。
     * <p>
     * 使用参数：{@link #API_KEY}、{@link #BASE_URL}、{@link #CHAT_MODEL}、{@link #EMBEDDING_MODEL}、
     * {@link #MEMORY_MAX_MESSAGES}、{@link #RAG_TOP_K}、{@link #DEMO_DOC_TEXT}。
     */
    @BeforeEach
    void init() {
        // 1. 构建 OpenAI 对话 / 向量模型（apiKey、baseUrl、model 写在 options 上）
        chatModel = OpenAiChatModel.builder()
                .options(OpenAiChatOptions.builder()
                        .apiKey(API_KEY)
                        .baseUrl(BASE_URL)
                        .model(CHAT_MODEL)
                        .build())
                .build();
        embeddingModel = OpenAiEmbeddingModel.builder()
                .options(OpenAiEmbeddingOptions.builder()
                        .apiKey(API_KEY)
                        .baseUrl(BASE_URL)
                        .model(EMBEDDING_MODEL)
                        .dimensions(EMBEDDING_DIM)
                        .build())
                .build();

        // 2. 内存向量库写入一条演示知识（会调用 Embedding API）
        vectorStore = SimpleVectorStore.builder(embeddingModel).build();
        vectorStore.add(List.of(new Document(DEMO_DOC_TEXT, Map.of("knowledge", "demo"))));

        // 3. ChatClient 挂上记忆 Advisor + RAG Advisor
        chatClient = ChatClient.builder(chatModel)
                .defaultSystem("你是助手，有知识库时优先依据知识库回答。")
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(
                                MessageWindowChatMemory.builder().maxMessages(MEMORY_MAX_MESSAGES).build()).build(),
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .searchRequest(SearchRequest.builder().topK(RAG_TOP_K).build())
                                .build(),
                        SimpleLoggerAdvisor.builder().build())
                .build();
    }

    private static String envOr(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    /**
     * 测什么：ChatModel 非流式 call 是否通。
     * <p>
     * 怎么测：发一句「你好」，看日志里的回复文本。
     * <p>
     * 参数：依赖 {@link #BASE_URL}、{@link #CHAT_MODEL}；无额外入参。
     * <p>
     * 预期：日志打印模型回复，无异常即通过。
     */
    @Test
    @DisplayName("ChatModel 非流式 call")
    void test_call() {
        var resp = chatModel.call(new Prompt("你好"));
        log.info("call => {}", resp.getResult().getOutput().getText());
    }

    /**
     * 测什么：ChatModel 流式 stream 是否通。
     * <p>
     * 怎么测：stream「你好」，把分片拼成完整字符串后打印。
     * <p>
     * 参数：同 call；超时 {@code Duration.ofMinutes(1)}。
     * <p>
     * 预期：拼出非空回复文本。
     */
    @Test
    @DisplayName("ChatModel 流式 stream")
    void test_stream() {
        String text = chatModel.stream(new Prompt("你好"))
                .map(r -> r.getResult().getOutput().getText())
                .reduce("", String::concat)
                .block(Duration.ofMinutes(1));
        log.info("stream => {}", text);
    }

    /**
     * 测什么：MessageChatMemoryAdvisor 按 conversationId 记住上文。
     * <p>
     * 怎么测：
     * <ol>
     *   <li>同一 conversationId={@code c1}，先说「我叫张三」</li>
     *   <li>再问「我叫什么？」——应能答出张三</li>
     * </ol>
     * <p>
     * 参数：
     * <ul>
     *   <li>advisor 参数 {@link ChatMemory#CONVERSATION_ID} = {@code c1}（两轮必须相同）</li>
     *   <li>窗口大小见 {@link #MEMORY_MAX_MESSAGES}</li>
     * </ul>
     */
    @Test
    @DisplayName("对话记忆（同 conversationId 两轮）")
    void test_memory() {
        String conversationId = "c1";
        log.info("r1 => {}", chatClient.prompt().user("我叫张三")
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId)).call().content());
        log.info("r2 => {}", chatClient.prompt().user("我叫什么？")
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId)).call().content());
    }

    /**
     * 测什么：SimpleVectorStore + QuestionAnswerAdvisor 的 RAG 通路。
     * <p>
     * 怎么测：init 已写入 {@link #DEMO_DOC_TEXT}；再问「张三几岁？」，回答应提到 28。
     * <p>
     * 参数：
     * <ul>
     *   <li>检索 topK = {@link #RAG_TOP_K}</li>
     *   <li>conversationId = {@code rag-1}（仅隔离会话，与检索无关）</li>
     * </ul>
     */
    @Test
    @DisplayName("内存向量 RAG")
    void test_rag() {
        log.info("rag => {}", chatClient.prompt().user("张三几岁？")
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, "rag-1")).call().content());
    }

    /**
     * 测什么：MilvusVectorStore 写入文档后，经 QuestionAnswerAdvisor 做 RAG。
     * <p>
     * 前置：Milvus 已启动（{@link #MILVUS_HOST}:{@link #MILVUS_PORT}），鉴权账号见常量。
     * <p>
     * 怎么测：
     * <ol>
     *   <li>连接并初始化集合 {@link #MILVUS_COLLECTION}</li>
     *   <li>写入「hei-ddd-ai-lite 是 Spring AI 脚手架。」</li>
     *   <li>提问「hei-ddd-ai-lite 是什么？」看是否答到脚手架相关内容</li>
     * </ol>
     * <p>
     * 参数：{@link #MILVUS_HOST}、{@link #MILVUS_PORT}、{@link #MILVUS_USERNAME}、
     * {@link #MILVUS_PASSWORD}、{@link #MILVUS_COLLECTION}、{@link #EMBEDDING_DIM}、
     * 检索 topK={@link #RAG_TOP_K}。
     */
    @Test
    @DisplayName("Milvus 向量 RAG")
    void test_milvus() throws Exception {
        // 1. 带鉴权连接 Milvus（用户名/密码必填）
        MilvusServiceClient milvus = new MilvusServiceClient(ConnectParam.newBuilder()
                .withHost(MILVUS_HOST)
                .withPort(MILVUS_PORT)
                .withAuthorization(MILVUS_USERNAME, MILVUS_PASSWORD)
                .build());
        try {
            // 2. 建 / 连集合
            MilvusVectorStore store = MilvusVectorStore.builder(milvus, embeddingModel)
                    .collectionName(MILVUS_COLLECTION)
                    .embeddingDimension(EMBEDDING_DIM)
                    .indexType(IndexType.IVF_FLAT)
                    .metricType(MetricType.COSINE)
                    .initializeSchema(true)
                    .build();
            store.afterPropertiesSet();
            // 3. 写入一条知识
            store.add(List.of(new Document("hei-ddd-ai-lite 是 Spring AI 脚手架。",
                    Map.of("knowledge", "demo"))));
            // 4. RAG 提问
            String answer = ChatClient.builder(chatModel)
                    .defaultAdvisors(QuestionAnswerAdvisor.builder(store)
                            .searchRequest(SearchRequest.builder().topK(RAG_TOP_K).build())
                            .build())
                    .build()
                    .prompt().user("hei-ddd-ai-lite 是什么？").call().content();
            log.info("milvus => {}", answer);
        } finally {
            milvus.close();
        }
    }

    /**
     * 测什么：把 MCP 工具挂到 ChatClient，模型能看到并描述工具。
     * <p>
     * 前置：
     * <ul>
     *   <li>已在 {@code ai-mcp-demo} 执行 {@code mvn package}，stdio 可拉起 fat JAR</li>
     *   <li>{@link #MCP_SSE_URL} 上已启动 SSE Profile 的同工程 MCP Server</li>
     * </ul>
     * <p>
     * 怎么测：提问「有哪些工具？」，日志中应出现工具列表相关回答。
     * <p>
     * 参数：{@link #MCP_FS_ROOT}（沙箱根）、{@link #MCP_SSE_URL}（SSE 地址）。
     */
    @Test
    @DisplayName("MCP 工具挂载")
    void test_mcp() throws Exception {
        // 1. 解析相邻工程 fat JAR，stdio 子进程与 SSE 共用同一套 filesystem 工具
        Path jar = resolveAiMcpDemoJar();
        if (!Files.isRegularFile(jar)) {
            throw new IllegalStateException("找不到 ai-mcp-demo JAR: " + jar
                    + "，请先在 ai-mcp-demo 下 mvn package");
        }
        Files.createDirectories(Path.of(MCP_FS_ROOT));

        var stdioParams = ServerParameters.builder("java")
                .args(
                        "-jar", jar.toAbsolutePath().toString(),
                        "--spring.profiles.active=stdio",
                        "--ai.mcp.filesystem.roots=" + MCP_FS_ROOT)
                .build();
        try (McpSyncClient stdio = McpClient.sync(
                        new StdioClientTransport(stdioParams, new JacksonMcpJsonMapperSupplier().get()))
                .requestTimeout(MCP_TIMEOUT).build();
                McpSyncClient sse = McpClient.sync(HttpClientSseClientTransport.builder(MCP_SSE_URL).build())
                        .requestTimeout(MCP_TIMEOUT).build()) {
            // 2. 双传输都 initialize，证明 SSE / stdio 均可握手
            stdio.initialize();
            sse.initialize();
            // 3. ChatClient 只挂一套工具：stdio+SSE 同源四工具名相同，不能重复注册
            String answer = ChatClient.builder(chatModel)
                    .defaultTools(new SyncMcpToolCallbackProvider(sse))
                    .build()
                    .prompt().user("有哪些工具？请用英文原名列出。").call().content();
            log.info("mcp => {}", answer);
        }
    }

    /**
     * 相对 app 模块目录解析 ai-mcp-demo 可执行 JAR。
     */
    private static Path resolveAiMcpDemoJar() {
        Path fromModule = Path.of("..", "ai-mcp-demo", "target", "ai-mcp-demo-1.0-SNAPSHOT.jar")
                .toAbsolutePath().normalize();
        if (Files.isRegularFile(fromModule)) {
            return fromModule;
        }
        return Path.of("ai-mcp-demo", "target", "ai-mcp-demo-1.0-SNAPSHOT.jar")
                .toAbsolutePath().normalize();
    }
}
