package io.github.jiangbyte.aimcp.demo.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallback;

/**
 * 用不同传输协议联调 {@code ai-mcp-demo}。
 *
 * <h2>怎么跑</h2>
 * <pre>
 *   # 1) 先打包被测 Server
 *   cd ../ai-mcp-demo && mvn -q package -DskipTests
 *
 *   # 2) SSE：另开终端启动 Server
 *   java -jar ../ai-mcp-demo/target/ai-mcp-demo-1.0-SNAPSHOT.jar \
 *     --ai.mcp.filesystem.roots=/tmp/ai-mcp-demo-sandbox
 *
 *   # 3) 跑本模块测试
 *   cd ../ai-mcp-demo-test && mvn -q test
 * </pre>
 *
 * <h2>可调常量</h2>
 * <ul>
 *   <li>{@link #SSE_BASE_URL} — SSE 服务根地址</li>
 *   <li>{@link #SANDBOX_ROOT} — 允许根（须与 Server 启动参数一致）</li>
 *   <li>{@link #REQUEST_TIMEOUT} — 客户端超时</li>
 * </ul>
 */
@Slf4j
class AiMcpDemoProtocolTest {

    /** SSE MCP baseUrl（对应 ai-mcp-demo application-sse.yml 的 server.port） */
    static final String SSE_BASE_URL = "http://127.0.0.1:8101";

    /** 沙箱目录：SSE 启动参数与 stdio 子进程参数都应指向这里 */
    static final Path SANDBOX_ROOT = Path.of("/tmp/ai-mcp-demo-sandbox");

    static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(60);

    /** 仓库根（含 ai-mcp-demo / ai-mcp-demo-test 目录） */
    static final Path REPO_ROOT = Path.of("..").toAbsolutePath().normalize();

    /**
     * 测什么：SSE 协议下 initialize、listTools、调用 filesystem 工具。
     * <p>
     * 前置：本机已启动 ai-mcp-demo（sse profile），且 roots={@link #SANDBOX_ROOT}。
     * <p>
     * 参数：{@link #SSE_BASE_URL}、{@link #SANDBOX_ROOT}。
     */
    @Test
    @DisplayName("SSE：listTools + list/read/write")
    void test_sse_protocol() throws Exception {
        assumeTrue(sseReachable(SSE_BASE_URL), () ->
                "SSE 不可达: " + SSE_BASE_URL + "，请先启动 ai-mcp-demo（默认 sse）");
        prepareSandbox();

        try (McpSyncClient client = McpDemoClients.sse(SSE_BASE_URL, REQUEST_TIMEOUT)) {
            assertFilesystemTools(client);
            assertSpringAiToolBridge(client);
        }
    }

    /**
     * 测什么：stdio 协议下由测试进程拉起 JAR，完成同样工具调用。
     * <p>
     * 前置：已 {@code mvn package} 产出
     * {@code ai-mcp-demo/target/ai-mcp-demo-1.0-SNAPSHOT.jar}。
     * <p>
     * 参数：JAR 路径由 {@link McpDemoClients#resolveDemoJar} 解析；沙箱={@link #SANDBOX_ROOT}。
     */
    @Test
    @DisplayName("STDIO：子进程拉起 JAR + list/read/write")
    void test_stdio_protocol() throws Exception {
        Path jar = McpDemoClients.resolveDemoJar(REPO_ROOT);
        assumeTrue(Files.isRegularFile(jar), () ->
                "找不到 JAR: " + jar + "，请先在 ai-mcp-demo 下 mvn package");
        prepareSandbox();

        try (McpSyncClient client = McpDemoClients.stdio(jar, SANDBOX_ROOT, REQUEST_TIMEOUT)) {
            assertFilesystemTools(client);
            assertSpringAiToolBridge(client);
        }
    }

    /**
     * 对任意协议客户端执行同一套工具断言。
     */
    private void assertFilesystemTools(McpSyncClient client) throws Exception {
        // 1. listTools：应包含四个核心工具
        List<String> names = McpDemoClients.toolNames(client);
        log.info("tools => {}", names);
        assertTrue(names.containsAll(List.of(
                "list_allowed_directories",
                "list_directory",
                "read_text_file",
                "write_file")), "工具列表不完整: " + names);

        // 2. list_allowed_directories
        CallToolResult allowed = McpDemoClients.callTool(client, "list_allowed_directories", Map.of());
        assertFalse(Boolean.TRUE.equals(allowed.isError()), McpDemoClients.textOf(allowed));
        String allowedText = McpDemoClients.textOf(allowed);
        log.info("allowed => {}", allowedText);
        assertTrue(allowedText.contains(SANDBOX_ROOT.toString())
                        || allowedText.contains(SANDBOX_ROOT.toRealPath().toString()),
                "允许根未包含沙箱: " + allowedText);

        // 3. list_directory
        CallToolResult listing = McpDemoClients.callTool(client, "list_directory",
                Map.of("path", SANDBOX_ROOT.toString()));
        assertFalse(Boolean.TRUE.equals(listing.isError()), McpDemoClients.textOf(listing));
        log.info("list => {}", McpDemoClients.textOf(listing));
        assertTrue(McpDemoClients.textOf(listing).contains("hello.txt"));

        // 4. read_text_file
        Path hello = SANDBOX_ROOT.resolve("hello.txt");
        CallToolResult read = McpDemoClients.callTool(client, "read_text_file",
                Map.of("path", hello.toString()));
        assertFalse(Boolean.TRUE.equals(read.isError()), McpDemoClients.textOf(read));
        assertTrue(McpDemoClients.textOf(read).contains("hello-mcp"));

        // 5. write_file + 再读回
        Path out = SANDBOX_ROOT.resolve("written-by-test.txt");
        CallToolResult write = McpDemoClients.callTool(client, "write_file",
                Map.of("path", out.toString(), "content", "from-protocol-test"));
        assertFalse(Boolean.TRUE.equals(write.isError()), McpDemoClients.textOf(write));
        CallToolResult readBack = McpDemoClients.callTool(client, "read_text_file",
                Map.of("path", out.toString()));
        assertTrue(McpDemoClients.textOf(readBack).contains("from-protocol-test"));
    }

    /**
     * 验证 Spring AI SyncMcpToolCallbackProvider 能从该连接拿到 ToolCallback。
     */
    private void assertSpringAiToolBridge(McpSyncClient client) {
        ToolCallback[] callbacks = McpDemoClients.asSpringAiTools(client);
        assertNotNull(callbacks);
        assertTrue(callbacks.length >= 4, "Spring AI ToolCallback 数量不足: " + callbacks.length);
        log.info("spring-ai toolCallbacks => {}", callbacks.length);
    }

    private void prepareSandbox() throws Exception {
        Files.createDirectories(SANDBOX_ROOT);
        Path hello = SANDBOX_ROOT.resolve("hello.txt");
        if (!Files.isRegularFile(hello)) {
            Files.writeString(hello, "hello-mcp\n", StandardCharsets.UTF_8);
        }
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
