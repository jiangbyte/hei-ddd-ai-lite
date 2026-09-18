package io.github.jiangbyte.aimcp.demo;

import io.github.jiangbyte.aimcp.demo.config.FilesystemProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * MCP Server 演示入口。
 * <p>
 * 用 Spring profile 切换传输协议：
 * <ul>
 *   <li>{@code sse}（默认）：HTTP SSE，端口见 application-sse.yml</li>
 *   <li>{@code stdio}：标准输入输出，供 Cursor 等以子进程方式拉起</li>
 * </ul>
 */
@SpringBootApplication
@EnableConfigurationProperties(FilesystemProperties.class)
public class AiMcpDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiMcpDemoApplication.class, args);
    }
}
