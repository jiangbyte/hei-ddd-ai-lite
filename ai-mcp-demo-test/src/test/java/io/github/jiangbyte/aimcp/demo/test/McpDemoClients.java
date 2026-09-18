package io.github.jiangbyte.aimcp.demo.test;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapperSupplier;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;

/**
 * 连接 ai-mcp-demo 的客户端辅助方法（SSE / stdio）。
 */
@UtilityClass
public class McpDemoClients {

    /**
     * 连接已启动的 SSE MCP Server。
     *
     * @param baseUrl 例如 http://127.0.0.1:8101
     */
    public static McpSyncClient sse(String baseUrl, Duration timeout) {
        McpSyncClient client = McpClient.sync(HttpClientSseClientTransport.builder(baseUrl).build())
                .requestTimeout(timeout)
                .build();
        client.initialize();
        return client;
    }

    /**
     * 以子进程方式拉起 ai-mcp-demo JAR（stdio profile）。
     *
     * @param jarPath     ai-mcp-demo 可执行 JAR
     * @param sandboxRoot 传给 --ai.mcp.filesystem.roots
     */
    public static McpSyncClient stdio(Path jarPath, Path sandboxRoot, Duration timeout) {
        var params = ServerParameters.builder("java")
                .args(
                        "-jar", jarPath.toAbsolutePath().toString(),
                        "--spring.profiles.active=stdio",
                        "--ai.mcp.filesystem.roots=" + sandboxRoot.toAbsolutePath())
                .build();
        McpSyncClient client = McpClient.sync(
                        new StdioClientTransport(params, new JacksonMcpJsonMapperSupplier().get()))
                .requestTimeout(timeout)
                .build();
        client.initialize();
        return client;
    }

    /** 用 Spring AI 把 MCP 工具适配成 ToolCallback[]（便于挂到 ChatClient） */
    public static ToolCallback[] asSpringAiTools(McpSyncClient client) {
        return new SyncMcpToolCallbackProvider(client).getToolCallbacks();
    }

    public static CallToolResult callTool(McpSyncClient client, String name, Map<String, Object> args) {
        return client.callTool(CallToolRequest.builder().name(name).arguments(args).build());
    }

    public static String textOf(CallToolResult result) {
        if (result == null || result.content() == null) {
            return "";
        }
        return result.content().stream()
                .filter(TextContent.class::isInstance)
                .map(TextContent.class::cast)
                .map(TextContent::text)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("\n"));
    }

    public static List<String> toolNames(McpSyncClient client) {
        McpSchema.ListToolsResult listed = client.listTools();
        return listed.tools().stream().map(McpSchema.Tool::name).toList();
    }

    public static Path resolveDemoJar(Path projectRootHint) {
        Path jar = projectRootHint.resolve("ai-mcp-demo/target/ai-mcp-demo-1.0-SNAPSHOT.jar");
        if (Files.isRegularFile(jar)) {
            return jar;
        }
        Path sibling = Path.of("..").resolve("ai-mcp-demo/target/ai-mcp-demo-1.0-SNAPSHOT.jar").normalize();
        if (Files.isRegularFile(sibling)) {
            return sibling.toAbsolutePath();
        }
        return jar.toAbsolutePath();
    }
}
