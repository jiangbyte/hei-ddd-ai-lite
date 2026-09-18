package io.github.jiangbyte.aimcp.demo.config;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 文件系统沙箱配置：工具只能访问配置的根目录及其子路径。
 * <p>
 * 配置前缀 {@code ai.mcp.filesystem}，例如：
 * <pre>
 * ai.mcp.filesystem.roots:
 *   - /home/charlie
 * </pre>
 * 也可用启动参数覆盖：{@code --ai.mcp.filesystem.roots=/tmp/sandbox}
 */
@Data
@ConfigurationProperties(prefix = "ai.mcp.filesystem")
public class FilesystemProperties {

    /** 允许访问的根目录列表（绝对路径） */
    private List<String> roots = new ArrayList<>();

    /**
     * 解析为 Path 列表；空列表表示未配置允许根。
     */
    public List<Path> resolveRoots() {
        return roots.stream()
                .filter(r -> r != null && !r.isBlank())
                .map(r -> Path.of(r.trim()).toAbsolutePath().normalize())
                .distinct()
                .toList();
    }
}
