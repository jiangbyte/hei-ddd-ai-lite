# ai-mcp-demo-test

独立测试工程：用 **SSE** / **stdio** 客户端联调同仓库下的 [`ai-mcp-demo`](../ai-mcp-demo)，并可用 **OpenAI**（或兼容端点）验证 LLM 是否识别/调用 MCP 工具。

> **不是** 父工程 `<module>`，在本目录单独 `mvn test`。

## 测什么

| 用例 | 类型 | 说明 |
|------|------|------|
| `AiMcpDemoProtocolTest#test_sse_protocol` | 协议 | 连接已启动 SSE Server，list/read/write |
| `AiMcpDemoProtocolTest#test_stdio_protocol` | 协议 | 测试进程拉起 stdio JAR，list/read/write |
| `AiMcpDemoLlmTest#test_stdio_llm_chat_model_lists_all_tools` | LLM | `ChatModel.call(Prompt)` 问「有哪些工具」，断言四个英文名都出现 |
| `AiMcpDemoLlmTest#test_stdio_llm_calls_list_allowed_directories` | LLM | 调用 `list_allowed_directories` |
| `AiMcpDemoLlmTest#test_stdio_llm_calls_read_text_file` | LLM | 调用 `read_text_file`，回答含 `hello-mcp` |
| `AiMcpDemoLlmTest#test_stdio_llm_calls_list_directory` | LLM | 调用 `list_directory` |
| `AiMcpDemoLlmTest#test_stdio_llm_calls_write_file` | LLM | 调用 `write_file` 并核对落盘 |
| `AiMcpDemoLlmTest#test_sse_llm_calls_read_text_file` | LLM | 同上读文件，走 SSE（需先起 Server） |

协议用例不依赖大模型。LLM 用例默认 `gpt-5-mini`；缺 `OPENAI_API_KEY` / JAR / SSE 时 **Assumption skip**。

LLM 用例通过 `CountingToolCallback` 统计真实工具调用次数，避免只看模型「嘴上说调用了」。

## 可调参数

协议见 `AiMcpDemoProtocolTest`；LLM 见 `AiMcpDemoLlmTest`：

| 常量 / 环境变量 | 默认 | 含义 |
|------|------|------|
| `OPENAI_API_KEY` | （必填） | OpenAI API Key |
| `OPENAI_BASE_URL` | `https://api.openai.com` | OpenAI 或兼容端点 |
| `OPENAI_CHAT_MODEL` / `CHAT_MODEL` | `gpt-5-mini` | 对话模型（需支持 tool calling） |
| `SANDBOX_ROOT` | `/tmp/ai-mcp-demo-sandbox` | 与 Server roots 一致 |

## 怎么跑

**需要 JDK 21**（`java -version` / `mvn -v` 都是 21）。SSE 相关用例要**另开终端**起 Server；不要把 `java -jar` 与 `mvn test` 粘在同一前台顺序执行。

```bash
# 终端 A：先 package，再起 SSE（保持运行）
cd ai-mcp-demo && mvn -q package -DskipTests
java -jar target/ai-mcp-demo-1.0-SNAPSHOT.jar \
  --ai.mcp.filesystem.roots=/tmp/ai-mcp-demo-sandbox

# 终端 B：测试（从仓库根，或从 demo 内 cd ../ai-mcp-demo-test）
cd ai-mcp-demo-test
export OPENAI_API_KEY=sk-...
# 可选：export OPENAI_BASE_URL=... OPENAI_CHAT_MODEL=gpt-5-mini
mvn -q test

# 只跑协议 / 只跑 LLM
mvn -q -Dtest=AiMcpDemoProtocolTest test
mvn -q -Dtest=AiMcpDemoLlmTest test
```

## 源码

```
ai-mcp-demo-test/
├── pom.xml
├── README.md
└── src/test/java/.../test/
    ├── McpDemoClients.java           # SSE/stdio 建连 + Spring AI 桥接
    ├── CountingToolCallback.java     # 统计 LLM 真实工具调用
    ├── AiMcpDemoProtocolTest.java    # 协议联调（无 LLM）
    └── AiMcpDemoLlmTest.java         # OpenAI + MCP 工具识别/调用
```
