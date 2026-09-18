package io.github.jiangbyte.aimcp.demo.tools;

import io.github.jiangbyte.aimcp.demo.config.FilesystemProperties;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * filesystem 风格 MCP 工具（对齐 server-filesystem 核心子集）。
 * <p>
 * 使用 Spring AI {@link McpTool} 注册工具，返回 MCP SDK 的 {@link CallToolResult}。
 * 所有路径必须落在 {@link FilesystemProperties#getRoots()} 配置的沙箱内。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FilesystemTools {

    private final FilesystemProperties properties;

    /**
     * 返回当前允许访问的根目录列表，方便客户端先确认沙箱范围。
     */
    @McpTool(
            name = "list_allowed_directories",
            description = "列出本 MCP 允许访问的根目录（沙箱）",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, openWorldHint = false))
    public CallToolResult listAllowedDirectories() {
        List<String> roots = properties.resolveRoots().stream().map(Path::toString).toList();
        return ok(String.join("\n", roots));
    }

    /**
     * 列出目录下的直接子项（文件与子目录名）。
     */
    @McpTool(
            name = "list_directory",
            description = "列出指定目录中的条目（文件名 / 子目录名）",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, openWorldHint = false))
    public CallToolResult listDirectory(
            @McpToolParam(description = "目录绝对或相对路径（须在允许根内）", required = true) String path) {
        try {
            // 1. 解析并校验路径落在沙箱内
            Path dir = resolveAllowed(path);
            // 2. 确认是目录
            if (!Files.isDirectory(dir)) {
                return error("不是目录: " + dir);
            }
            // 3. 列出直接子项并排序返回
            try (Stream<Path> stream = Files.list(dir)) {
                String listing = stream
                        .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                        .map(p -> (Files.isDirectory(p) ? "[dir] " : "[file] ") + p.getFileName())
                        .collect(Collectors.joining("\n"));
                return ok(listing.isBlank() ? "(空目录)" : listing);
            }
        } catch (Exception e) {
            log.warn("list_directory 失败 path={}", path, e);
            return error(e.getMessage());
        }
    }

    /**
     * 以 UTF-8 读取文本文件全文。
     */
    @McpTool(
            name = "read_text_file",
            description = "读取文本文件内容（UTF-8）",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, openWorldHint = false))
    public CallToolResult readTextFile(
            @McpToolParam(description = "文件绝对或相对路径（须在允许根内）", required = true) String path) {
        try {
            // 1. 沙箱校验
            Path file = resolveAllowed(path);
            // 2. 确认是普通文件
            if (!Files.isRegularFile(file)) {
                return error("不是普通文件: " + file);
            }
            // 3. 读取全文
            return ok(Files.readString(file, StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.warn("read_text_file 失败 path={}", path, e);
            return error(e.getMessage());
        }
    }

    /**
     * 写入（覆盖）文本文件；父目录不存在时自动创建。
     */
    @McpTool(
            name = "write_file",
            description = "写入文本文件（UTF-8，覆盖已存在内容；必要时创建父目录）",
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = false,
                    destructiveHint = true,
                    idempotentHint = true,
                    openWorldHint = false))
    public CallToolResult writeFile(
            @McpToolParam(description = "目标文件路径（须在允许根内）", required = true) String path,
            @McpToolParam(description = "要写入的文本内容", required = true) String content) {
        try {
            // 1. 沙箱校验目标路径
            Path file = resolveAllowed(path);
            // 2. 确保父目录存在
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            // 3. 写入内容并返回确认信息
            String body = content == null ? "" : content;
            Files.writeString(file, body, StandardCharsets.UTF_8);
            return ok("已写入: " + file.toAbsolutePath().normalize() + " (" + Files.size(file) + " bytes)");
        } catch (Exception e) {
            log.warn("write_file 失败 path={}", path, e);
            return error(e.getMessage());
        }
    }

    /**
     * 将用户传入路径规范为绝对路径，并校验落在任一允许根之下。
     */
    private Path resolveAllowed(String rawPath) throws IOException {
        // 1. 规范化用户路径
        if (!StringUtils.hasText(rawPath)) {
            throw new IllegalArgumentException("path 不能为空");
        }
        Path target = Path.of(rawPath.trim()).toAbsolutePath().normalize();

        // 2. 读取允许根；未配置则拒绝一切访问
        List<Path> roots = properties.resolveRoots();
        if (roots.isEmpty()) {
            throw new IllegalStateException("未配置 ai.mcp.filesystem.roots，拒绝访问");
        }

        // 3. 已存在则解析真实路径，再做前缀校验（防 ../ 越界）
        Path check = Files.exists(target) ? target.toRealPath() : target;
        for (Path root : roots) {
            Path realRoot = Files.exists(root) ? root.toRealPath() : root;
            if (check.equals(realRoot) || check.startsWith(realRoot)) {
                return check;
            }
        }
        throw new IllegalArgumentException("路径越出允许根: " + target + "；允许根=" + roots);
    }

    /** 成功结果：MCP SDK CallToolResult 文本内容 */
    private static CallToolResult ok(String text) {
        return CallToolResult.builder().addTextContent(text == null ? "" : text).isError(false).build();
    }

    /** 失败结果：isError=true，客户端可识别工具调用失败 */
    private static CallToolResult error(String message) {
        return CallToolResult.builder()
                .addTextContent(message == null ? "unknown error" : message)
                .isError(true)
                .build();
    }
}
