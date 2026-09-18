package io.github.jiangbyte.hei.bootstrap;

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
 * AI 基础能力冒烟测试（单文件）。
 *
 * <h2>怎么跑</h2>
 * <pre>
 *   export JAVA_HOME=/path/to/jdk-21
 *   mvn -pl bootstrap -Dtest=AiAgentTest#test_call test
 *   # 或 IDE 中直接点方法旁的绿色三角
 * </pre>
 *
 * <h2>先改这些常量（按本机环境）</h2>
 * <ul>
 *   <li>{@link #BASE_URL} / {@link #API_KEY} / {@link #CHAT_MODEL} / {@link #EMBEDDING_MODEL}
 *       — OpenAI 兼容网关；所有对话与向量化都用它们</li>
 *   <li>{@link #MILVUS_HOST} / {@link #MILVUS_PORT} / {@link #MILVUS_COLLECTION}
 *       — 仅 {@link #test_milvus()} 需要；本机需已启动 Milvus</li>
 *   <li>{@link #MCP_FS_ROOT} / {@link #MCP_SSE_URL}
 *       — 仅 {@link #test_mcp()} 需要；stdio 要能跑 npx，SSE 地址要有 MCP 服务</li>
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

    // ========================= 可调参数（OpenAI 兼容） =========================

    /** 网关根地址，不要带 /v1；SDK 会拼 completions / embeddings 路径 */
    static final String BASE_URL = "https://xxx";
    /** 网关 API Key */
    static final String API_KEY = "sk-xxx";
    /** 对话模型名（网关侧实际可用的 model id） */
    static final String CHAT_MODEL = "gpt-4.1-mini";
    /** 向量模型名；维度需与 Milvus embeddingDimension 一致（此处按 1536） */
    static final String EMBEDDING_MODEL = "text-embedding-3-small";

    // ========================= 可调参数（Milvus，仅 test_milvus） =========================

    /** Milvus 主机 */
    static final String MILVUS_HOST = "127.0.0.1";
    /** Milvus 端口，默认 19530 */
    static final int MILVUS_PORT = 19530;
    /** 测试用集合名；不存在且 initializeSchema=true 时会自动建 */
    static final String MILVUS_COLLECTION = "hei_ai_agent_test";
    /** 向量维度，需与 EMBEDDING_MODEL 输出一致 */
    static final int EMBEDDING_DIM = 1536;

    // ========================= 可调参数（MCP，仅 test_mcp） =========================

    /** filesystem MCP 允许访问的根目录（stdio 启动参数） */
    static final String MCP_FS_ROOT = System.getProperty("user.home");
    /** SSE MCP 服务 baseUrl，例如 http://127.0.0.1:8101 */
    static final String MCP_SSE_URL = "http://127.0.0.1:8101";

    // ========================= 固定演示数据（一般不用改） =========================

    /** 记忆窗口：最多保留多少条消息 */
    static final int MEMORY_MAX_MESSAGES = 20;
    /** RAG 检索条数 topK */
    static final int RAG_TOP_K = 3;
    /** 写入向量库的演示文档正文（test_rag 会问「王大瓜几岁」） */
    static final String DEMO_DOC_TEXT = "王大瓜今年 28 岁，做 Java 与 Spring AI。";

    private ChatModel chatModel;
    private EmbeddingModel embeddingModel;
    private VectorStore vectorStore;
    private ChatClient chatClient;

    /**
     * 每个用例前准备：ChatModel、EmbeddingModel、内存向量库、带记忆+RAG 的 ChatClient。
     * <p>
     * 使用参数：{@link #BASE_URL}、{@link #API_KEY}、{@link #CHAT_MODEL}、{@link #EMBEDDING_MODEL}、
     * {@link #MEMORY_MAX_MESSAGES}、{@link #RAG_TOP_K}、{@link #DEMO_DOC_TEXT}。
     */
    @BeforeEach
    void init() {
        // 1. 构建对话与向量模型
        chatModel = OpenAiChatModel.builder()
                .options(OpenAiChatOptions.builder()
                        .baseUrl(BASE_URL).apiKey(API_KEY).model(CHAT_MODEL).build())
                .build();
        embeddingModel = OpenAiEmbeddingModel.builder()
                .options(OpenAiEmbeddingOptions.builder()
                        .baseUrl(BASE_URL).apiKey(API_KEY).model(EMBEDDING_MODEL).build())
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

    /**
     * 测什么：ChatModel 非流式 call 是否通。
     * <p>
     * 怎么测：发一句「你好」，看日志里的回复文本。
     * <p>
     * 参数：依赖 {@link #BASE_URL}、{@link #API_KEY}、{@link #CHAT_MODEL}；无额外入参。
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
     *   <li>同一 conversationId={@code c1}，先说「我叫王大瓜」</li>
     *   <li>再问「我叫什么？」——应能答出王大瓜</li>
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
        log.info("r1 => {}", chatClient.prompt().user("我叫王大瓜")
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId)).call().content());
        log.info("r2 => {}", chatClient.prompt().user("我叫什么？")
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId)).call().content());
    }

    /**
     * 测什么：SimpleVectorStore + QuestionAnswerAdvisor 的 RAG 通路。
     * <p>
     * 怎么测：init 已写入 {@link #DEMO_DOC_TEXT}；再问「王大瓜几岁？」，回答应提到 28。
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
        log.info("rag => {}", chatClient.prompt().user("王大瓜几岁？")
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, "rag-1")).call().content());
    }

    /**
     * 测什么：MilvusVectorStore 写入文档后，经 QuestionAnswerAdvisor 做 RAG。
     * <p>
     * 前置：本机 Milvus 已启动（{@link #MILVUS_HOST}:{@link #MILVUS_PORT}）。
     * <p>
     * 怎么测：
     * <ol>
     *   <li>连接并初始化集合 {@link #MILVUS_COLLECTION}</li>
     *   <li>写入「hei-ddd-ai-lite 是 Spring AI 脚手架。」</li>
     *   <li>提问「hei-ddd-ai-lite 是什么？」看是否答到脚手架相关内容</li>
     * </ol>
     * <p>
     * 参数：{@link #MILVUS_HOST}、{@link #MILVUS_PORT}、{@link #MILVUS_COLLECTION}、
     * {@link #EMBEDDING_DIM}、检索 topK={@link #RAG_TOP_K}。
     */
    @Test
    @DisplayName("Milvus 向量 RAG")
    void test_milvus() throws Exception {
        MilvusServiceClient milvus = new MilvusServiceClient(ConnectParam.newBuilder()
                .withHost(MILVUS_HOST).withPort(MILVUS_PORT).build());
        try {
            // 1. 建 / 连集合
            MilvusVectorStore store = MilvusVectorStore.builder(milvus, embeddingModel)
                    .collectionName(MILVUS_COLLECTION)
                    .embeddingDimension(EMBEDDING_DIM)
                    .indexType(IndexType.IVF_FLAT)
                    .metricType(MetricType.COSINE)
                    .initializeSchema(true)
                    .build();
            store.afterPropertiesSet();
            // 2. 写入一条知识
            store.add(List.of(new Document("hei-ddd-ai-lite 是 Spring AI 脚手架。",
                    Map.of("knowledge", "demo"))));
            // 3. RAG 提问
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
     *   <li>本机可执行 {@code npx}（stdio filesystem MCP）</li>
     *   <li>{@link #MCP_SSE_URL} 上有可连的 SSE MCP 服务</li>
     * </ul>
     * <p>
     * 怎么测：提问「有哪些工具？」，日志中应出现工具列表相关回答。
     * <p>
     * 参数：{@link #MCP_FS_ROOT}（filesystem 根目录）、{@link #MCP_SSE_URL}（SSE 地址）。
     */
    @Test
    @DisplayName("MCP 工具挂载")
    void test_mcp() {
        var stdioParams = ServerParameters.builder("npx")
                .args("-y", "@modelcontextprotocol/server-filesystem", MCP_FS_ROOT, MCP_FS_ROOT)
                .build();
        try (McpSyncClient stdio = McpClient.sync(
                        new StdioClientTransport(stdioParams, new JacksonMcpJsonMapperSupplier().get()))
                .requestTimeout(Duration.ofSeconds(30)).build();
                McpSyncClient sse = McpClient.sync(HttpClientSseClientTransport.builder(MCP_SSE_URL).build())
                        .requestTimeout(Duration.ofMinutes(1)).build()) {
            stdio.initialize();
            sse.initialize();
            String answer = ChatClient.builder(chatModel)
                    .defaultTools(new SyncMcpToolCallbackProvider(stdio, sse))
                    .build()
                    .prompt().user("有哪些工具？").call().content();
            log.info("mcp => {}", answer);
        }
    }
}
