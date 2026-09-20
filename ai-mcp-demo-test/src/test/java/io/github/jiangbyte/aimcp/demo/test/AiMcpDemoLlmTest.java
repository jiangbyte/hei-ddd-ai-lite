package io.github.jiangbyte.aimcp.demo.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import io.modelcontextprotocol.client.McpSyncClient;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.ToolCallback;

/**
 * 用 OpenAI（或兼容端点）验证：MCP 工具能被识别，并能被模型正常调用。
 *
 * <h2>前置</h2>
 * <ul>
 *   <li>已 package ai-mcp-demo fat JAR</li>
 *   <li>已设置 {@code OPENAI_API_KEY}（可选 {@code OPENAI_BASE_URL} / {@code OPENAI_CHAT_MODEL}）</li>
 *   <li>SSE 用例需另起 ai-mcp-demo（默认 sse + 同一沙箱）</li>
 * </ul>
 */
@Slf4j
class AiMcpDemoLlmTest {

    static final String API_KEY = envOr("OPENAI_API_KEY", "");
    static final String BASE_URL = envOr("OPENAI_BASE_URL", "https://api.openai.com");
    static final String CHAT_MODEL = envOr("OPENAI_CHAT_MODEL", "gpt-5-mini");
    static final String SSE_BASE_URL = "http://127.0.0.1:8101";
    static final Path SANDBOX_ROOT = Path.of("/tmp/ai-mcp-demo-sandbox");
    static final Duration MCP_TIMEOUT = Duration.ofSeconds(120);
    static final Path REPO_ROOT = Path.of("..").toAbsolutePath().normalize();

    static final List<String> ALL_TOOLS = List.of(
            "list_allowed_directories",
            "list_directory",
            "read_text_file",
            "write_file");

    /**
     * 测什么：ChatModel.call(Prompt) 挂上工具后，问「有哪些工具」能说出四个英文名。
     * <p>
     * 写法对齐参考：Prompt + UserMessage + chatModel.call，并打印 ChatResponse。
     */
    @Test
    @DisplayName("STDIO+LLM：ChatModel.call 识别全部工具名")
    void test_stdio_llm_chat_model_lists_all_tools() throws Exception {
        assumeOpenAi();
        Path jar = requireDemoJar();
        prepareSandbox();

        try (McpSyncClient client = McpDemoClients.stdio(jar, SANDBOX_ROOT, MCP_TIMEOUT)) {
            // 1. MCP → ToolCallback，确认四工具定义齐全
            ToolCallback[] tools = McpDemoClients.asSpringAiTools(client);
            assertToolDefinitions(tools);

            // 2. 参考写法：Prompt + ChatModel.call，工具挂在 ChatOptions.toolCallbacks
            ChatModel chatModel = openAiChatModel();
            Prompt prompt = Prompt.builder()
                    .messages(new UserMessage("""
                            有哪些工具可以使用
                            请用英文原名列出全部可用工具，不要编造，不要只写中文描述。
                            """))
                    .chatOptions(OpenAiChatOptions.builder()
                            .model(CHAT_MODEL)
                            .toolCallbacks(tools)
                            .build())
                    .build();

            ChatResponse chatResponse = chatModel.call(prompt);
            // 参考：打印 call 结果；无 fastjson 时用手写摘要，避免额外依赖
            log.info("测试结果(call):{}", simplify(chatResponse));

            // 3. 从回复文本中核对四个工具名都被提到
            String text = extractText(chatResponse);
            log.info("list-tools-answer => {}", text);
            assertListsAllToolNames(text);
        }
    }

    /**
     * 测什么：stdio 下模型会调用 list_allowed_directories。
     */
    @Test
    @DisplayName("STDIO+LLM：调用 list_allowed_directories")
    void test_stdio_llm_calls_list_allowed_directories() throws Exception {
        assumeOpenAi();
        Path jar = requireDemoJar();
        prepareSandbox();

        try (McpSyncClient client = McpDemoClients.stdio(jar, SANDBOX_ROOT, MCP_TIMEOUT)) {
            CountingToolCallback.Bundle bundle = CountingToolCallback.wrap(McpDemoClients.asSpringAiTools(client));
            assertToolDefinitions(bundle.getCallbacks());

            String answer = chatClient(bundle.getCallbacks()).prompt()
                    .user("请调用工具 list_allowed_directories，然后把返回的允许根目录路径原样告诉我。")
                    .call()
                    .content();
            log.info("allowed-via-llm => {}", answer);
            log.info("tool counters => {}", bundle.getCounters());

            assertTrue(count(bundle, "list_allowed_directories") >= 1,
                    "未调用 list_allowed_directories: " + bundle.getCounters());
            assertNotNull(answer);
            assertTrue(answer.contains(SANDBOX_ROOT.toString())
                            || answer.contains("ai-mcp-demo-sandbox"),
                    "回答未包含沙箱路径: " + answer);
        }
    }

    /**
     * 测什么：stdio 下模型会调用 read_text_file。
     */
    @Test
    @DisplayName("STDIO+LLM：调用 read_text_file")
    void test_stdio_llm_calls_read_text_file() throws Exception {
        assumeOpenAi();
        Path jar = requireDemoJar();
        prepareSandbox();

        try (McpSyncClient client = McpDemoClients.stdio(jar, SANDBOX_ROOT, MCP_TIMEOUT)) {
            CountingToolCallback.Bundle bundle = CountingToolCallback.wrap(McpDemoClients.asSpringAiTools(client));
            Path hello = SANDBOX_ROOT.resolve("hello.txt");
            String answer = chatClient(bundle.getCallbacks()).prompt()
                    .user("请调用工具 read_text_file，读取文件：" + hello
                            + " 。把文件原文内容原样告诉我，不要猜测。")
                    .call()
                    .content();
            log.info("read-via-llm => {}", answer);
            log.info("tool counters => {}", bundle.getCounters());

            assertTrue(count(bundle, "read_text_file") >= 1,
                    "未调用 read_text_file: " + bundle.getCounters());
            assertNotNull(answer);
            assertTrue(answer.toLowerCase(Locale.ROOT).contains("hello-mcp"),
                    "回答未包含 hello-mcp: " + answer);
        }
    }

    /**
     * 测什么：stdio 下模型会调用 list_directory。
     */
    @Test
    @DisplayName("STDIO+LLM：调用 list_directory")
    void test_stdio_llm_calls_list_directory() throws Exception {
        assumeOpenAi();
        Path jar = requireDemoJar();
        prepareSandbox();

        try (McpSyncClient client = McpDemoClients.stdio(jar, SANDBOX_ROOT, MCP_TIMEOUT)) {
            CountingToolCallback.Bundle bundle = CountingToolCallback.wrap(McpDemoClients.asSpringAiTools(client));
            String answer = chatClient(bundle.getCallbacks()).prompt()
                    .user("请调用工具 list_directory，列出目录：" + SANDBOX_ROOT
                            + " 下有哪些文件。回答里要包含真实文件名。")
                    .call()
                    .content();
            log.info("list-via-llm => {}", answer);
            log.info("tool counters => {}", bundle.getCounters());

            assertTrue(count(bundle, "list_directory") >= 1,
                    "未调用 list_directory: " + bundle.getCounters());
            assertNotNull(answer);
            assertTrue(answer.contains("hello.txt"), "回答未包含 hello.txt: " + answer);
        }
    }

    /**
     * 测什么：stdio 下模型会调用 write_file，并落盘可核对。
     */
    @Test
    @DisplayName("STDIO+LLM：调用 write_file")
    void test_stdio_llm_calls_write_file() throws Exception {
        assumeOpenAi();
        Path jar = requireDemoJar();
        prepareSandbox();

        Path out = SANDBOX_ROOT.resolve("written-by-llm.txt");
        Files.deleteIfExists(out);

        try (McpSyncClient client = McpDemoClients.stdio(jar, SANDBOX_ROOT, MCP_TIMEOUT)) {
            CountingToolCallback.Bundle bundle = CountingToolCallback.wrap(McpDemoClients.asSpringAiTools(client));
            String answer = chatClient(bundle.getCallbacks()).prompt()
                    .user("请调用工具 write_file：path=" + out
                            + " ，content 必须恰好是 from-llm-write 。写完后用一句话确认已写入。")
                    .call()
                    .content();
            log.info("write-via-llm => {}", answer);
            log.info("tool counters => {}", bundle.getCounters());

            assertTrue(count(bundle, "write_file") >= 1,
                    "未调用 write_file: " + bundle.getCounters());
            assertTrue(Files.isRegularFile(out), "落盘文件不存在: " + out);
            assertTrue(Files.readString(out, StandardCharsets.UTF_8).contains("from-llm-write"),
                    "文件内容不对: " + Files.readString(out, StandardCharsets.UTF_8));
        }
    }

    /**
     * 测什么：SSE 下模型调用 read_text_file（需已起 Server）。
     */
    @Test
    @DisplayName("SSE+LLM：调用 read_text_file")
    void test_sse_llm_calls_read_text_file() throws Exception {
        assumeOpenAi();
        assumeTrue(sseReachable(SSE_BASE_URL), () ->
                "SSE 不可达: " + SSE_BASE_URL + "，请先启动 ai-mcp-demo（默认 sse）");
        prepareSandbox();

        try (McpSyncClient client = McpDemoClients.sse(SSE_BASE_URL, MCP_TIMEOUT)) {
            CountingToolCallback.Bundle bundle = CountingToolCallback.wrap(McpDemoClients.asSpringAiTools(client));
            Path hello = SANDBOX_ROOT.resolve("hello.txt");
            String answer = chatClient(bundle.getCallbacks()).prompt()
                    .user("请调用工具 read_text_file，读取文件：" + hello
                            + " 。把文件原文内容原样告诉我，不要猜测。")
                    .call()
                    .content();
            log.info("sse-read-via-llm => {}", answer);
            log.info("tool counters => {}", bundle.getCounters());

            assertTrue(count(bundle, "read_text_file") >= 1,
                    "未调用 read_text_file: " + bundle.getCounters());
            assertTrue(answer != null && answer.toLowerCase(Locale.ROOT).contains("hello-mcp"),
                    "回答未包含 hello-mcp: " + answer);
        }
    }

    private ChatModel openAiChatModel() {
        return OpenAiChatModel.builder()
                .options(OpenAiChatOptions.builder()
                        .apiKey(API_KEY)
                        .baseUrl(BASE_URL)
                        .model(CHAT_MODEL)
                        .build())
                .build();
    }

    private ChatClient chatClient(ToolCallback[] tools) {
        return ChatClient.builder(openAiChatModel())
                .defaultSystem("你是助手。需要文件或目录信息时必须调用提供的工具，禁止编造文件内容。")
                .defaultTools(tools)
                .build();
    }

    private static int count(CountingToolCallback.Bundle bundle, String name) {
        return bundle.getCounters().getOrDefault(name, new AtomicInteger()).get();
    }

    private static void assertToolDefinitions(ToolCallback[] tools) {
        assertNotNull(tools);
        assertTrue(tools.length >= 4, "ToolCallback 数量不足: " + tools.length);
        Set<String> names = Arrays.stream(tools)
                .map(t -> t.getToolDefinition().name())
                .collect(Collectors.toCollection(HashSet::new));
        assertTrue(names.containsAll(ALL_TOOLS), "工具定义不完整: " + names);
    }

    private static void assertListsAllToolNames(String answer) {
        assertNotNull(answer);
        assertFalse(answer.isBlank(), "模型回复为空");
        String lower = answer.toLowerCase(Locale.ROOT);
        List<String> missing = ALL_TOOLS.stream()
                .filter(name -> !lower.contains(name.toLowerCase(Locale.ROOT)))
                .toList();
        assertTrue(missing.isEmpty(),
                "模型未完整列出工具名，缺少 " + missing + "，全文: " + answer);
    }

    private static String extractText(ChatResponse response) {
        if (response == null || response.getResult() == null || response.getResult().getOutput() == null) {
            return "";
        }
        String text = response.getResult().getOutput().getText();
        return text == null ? "" : text;
    }

    /**
     * 只保留便于阅读的字段，避免把整个 ChatResponse 图打爆日志。
     */
    private static Map<String, Object> simplify(ChatResponse response) {
        return Map.of(
                "text", extractText(response),
                "metadata", response == null || response.getMetadata() == null
                        ? Map.of()
                        : Map.of(
                        "model", String.valueOf(response.getMetadata().getModel()),
                        "id", String.valueOf(response.getMetadata().getId())));
    }

    private Path requireDemoJar() {
        Path jar = McpDemoClients.resolveDemoJar(REPO_ROOT);
        assumeTrue(Files.isRegularFile(jar), () ->
                "找不到 JAR: " + jar + "，请先在 ai-mcp-demo 下 mvn package");
        return jar;
    }

    private void assumeOpenAi() {
        assumeTrue(API_KEY != null && !API_KEY.isBlank(), () ->
                "未设置 OPENAI_API_KEY，跳过 LLM 用例（模型=" + CHAT_MODEL + "）");
    }

    private void prepareSandbox() throws Exception {
        Files.createDirectories(SANDBOX_ROOT);
        Path hello = SANDBOX_ROOT.resolve("hello.txt");
        Files.writeString(hello, "hello-mcp\n", StandardCharsets.UTF_8);
        assertFalse(Files.readString(hello).isBlank());
    }

    private static String envOr(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private static boolean sseReachable(String baseUrl) {
        try {
            HttpURLConnection conn = (HttpURLConnection) URI.create(baseUrl + "/sse").toURL().openConnection();
            conn.setConnectTimeout(1500);
            conn.setReadTimeout(1500);
            conn.setRequestMethod("GET");
            int code = conn.getResponseCode();
            conn.disconnect();
            return code > 0 && code < 500;
        } catch (Exception e) {
            return false;
        }
    }
}
