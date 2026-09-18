# ai-mcp-demo

教学用 **MCP Server** 小项目：用 Spring AI 暴露 filesystem 风格工具，并通过 **Spring profile** 在 **stdio** 与 **SSE** 两种传输之间切换。

> 本目录是独立 Maven 工程，**不是** `hei-ddd-ai-lite` 父 POM 的 `<module>`。请在本目录单独 `mvn` 构建。
>
> 协议联调请看同级 [`ai-mcp-demo-test`](../ai-mcp-demo-test)。

## MCP 是什么

Model Context Protocol（MCP）让 AI 客户端以统一方式发现并调用服务端提供的 **tools / resources / prompts**。

本 demo 只演示 **tools**：列目录、读文件、写文件（对齐 `@modelcontextprotocol/server-filesystem` 核心子集）。

## stdio vs SSE

| | stdio | SSE（本 demo 默认） |
|--|-------|---------------------|
| 通信 | 进程 stdin / stdout | HTTP：`GET /sse` + `POST /mcp/message` |
| 典型用法 | Cursor / Claude Desktop 拉起本地 JAR | 独立常驻进程，客户端连 `http://host:port` |
| 关键配置 | `spring.ai.mcp.server.stdio=true`，关掉 Web | `spring.ai.mcp.server.protocol=SSE` |
| 注意 | **禁止**日志打到 stdout | 端口默认 `8101` |

传输变了，工具代码不变——业务在 `FilesystemTools`，协议在 yaml。

## 技术栈

- Java 21、Spring Boot 4.1.1、Spring AI 2.0.0
- 依赖：`spring-ai-starter-mcp-server-webmvc`（SSE；stdio 用 profile 关闭 Web）

## 关键配置

| 配置项 | 含义 |
|--------|------|
| `spring.profiles.default` / `active` | `sse` 或 `stdio` |
| `spring.ai.mcp.server.name` | MCP 服务名 |
| `spring.ai.mcp.server.protocol` | SSE 模式下为 `SSE` |
| `spring.ai.mcp.server.stdio` | stdio 模式下为 `true` |
| `server.port` | SSE 端口，默认 `8101` |
| `ai.mcp.filesystem.roots` | 允许访问的根目录列表（沙箱） |

覆盖允许根示例：

```bash
java -jar target/ai-mcp-demo-1.0-SNAPSHOT.jar --ai.mcp.filesystem.roots=/tmp/sandbox
```

## 工具列表

| 工具名 | 说明 |
|--------|------|
| `list_allowed_directories` | 列出沙箱根 |
| `list_directory` | 列目录 |
| `read_text_file` | 读 UTF-8 文本 |
| `write_file` | 写 UTF-8 文本（可建父目录） |

越界路径会被拒绝。

## 构建与启动

**需要 JDK 21**（`java -version` 与 `mvn -v` 均须显示 21）。若默认是 17，先 `export JAVA_HOME=.../jdk-21` 再把 `$JAVA_HOME/bin` 放到 `PATH` 前面，否则会出现 `release version 21 not supported` 或 `UnsupportedClassVersionError`。

```bash
cd ai-mcp-demo
mvn -q package -DskipTests
```

### SSE（默认）

```bash
java -jar target/ai-mcp-demo-1.0-SNAPSHOT.jar
# 等价：--spring.profiles.active=sse
```

客户端（Java）示例：

```java
HttpClientSseClientTransport.builder("http://127.0.0.1:8101").build();
```

对应 `hei-ddd-ai-lite` 测试里可把 SSE baseUrl 配成 `http://127.0.0.1:8101`。

### STDIO

```bash
java -jar target/ai-mcp-demo-1.0-SNAPSHOT.jar --spring.profiles.active=stdio
```

Cursor / Claude `mcp.json` 示例：

```json
{
  "mcpServers": {
    "ai-mcp-demo": {
      "command": "java",
      "args": [
        "-jar",
        "/绝对路径/hei-ddd-ai-lite/ai-mcp-demo/target/ai-mcp-demo-1.0-SNAPSHOT.jar",
        "--spring.profiles.active=stdio",
        "--ai.mcp.filesystem.roots=/你允许的目录"
      ]
    }
  }
}
```

## 源码结构

```
ai-mcp-demo/
├── pom.xml
├── README.md
└── src/main/
    ├── java/.../aimcp/demo/
    │   ├── AiMcpDemoApplication.java
    │   ├── config/FilesystemProperties.java
    │   └── tools/FilesystemTools.java      # @McpTool
    └── resources/
        ├── application.yml
        ├── application-sse.yml
        └── application-stdio.yml
```

## 学习路径建议

1. 先看 `FilesystemTools`：`@McpTool` 如何变成可调用工具  
2. 再看 `application-sse.yml` / `application-stdio.yml`：同一 JAR 如何换传输  
3. 用 SSE 起服务，再用任意 MCP 客户端 `initialize` + `list_tools` 验证
