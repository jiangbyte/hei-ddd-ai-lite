package io.github.jiangbyte.aimcp.demo.test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;

/**
 * 包装 {@link ToolCallback}，统计各工具被 LLM 实际调用的次数。
 */
final class CountingToolCallback implements ToolCallback {

    private final ToolCallback delegate;
    private final Map<String, AtomicInteger> counters;

    private CountingToolCallback(ToolCallback delegate, Map<String, AtomicInteger> counters) {
        this.delegate = delegate;
        this.counters = counters;
    }

    /**
     * 包装结果：回调数组 + 共享计数表。
     */
    @Getter
    @RequiredArgsConstructor
    static final class Bundle {
        private final ToolCallback[] callbacks;
        private final Map<String, AtomicInteger> counters;
    }

    static Bundle wrap(ToolCallback[] source) {
        Map<String, AtomicInteger> counters = new ConcurrentHashMap<>();
        ToolCallback[] wrapped = new ToolCallback[source.length];
        for (int i = 0; i < source.length; i++) {
            ToolCallback cb = source[i];
            counters.putIfAbsent(cb.getToolDefinition().name(), new AtomicInteger());
            wrapped[i] = new CountingToolCallback(cb, counters);
        }
        return new Bundle(wrapped, counters);
    }

    @Override
    public ToolDefinition getToolDefinition() {
        return delegate.getToolDefinition();
    }

    @Override
    public ToolMetadata getToolMetadata() {
        return delegate.getToolMetadata();
    }

    @Override
    public String call(String toolInput) {
        // 1. 按工具名累加调用次数，证明 LLM 真正走到了 MCP 工具
        counters.get(delegate.getToolDefinition().name()).incrementAndGet();
        // 2. 转发到原始 MCP ToolCallback
        return delegate.call(toolInput);
    }

    @Override
    public String call(String toolInput, ToolContext toolContext) {
        counters.get(delegate.getToolDefinition().name()).incrementAndGet();
        return delegate.call(toolInput, toolContext);
    }
}
