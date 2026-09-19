package io.github.jiangbyte.hei.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 应用启动入口。
 * <p>
 * 仅扫描业务组件包；infrastructure 下的 AutoConfiguration 由 spring.factories / AutoConfiguration.imports 加载，
 * 避免可选依赖相关配置类被组件扫描强行注册。
 */
@SpringBootApplication(scanBasePackages = {
        "io.github.jiangbyte.hei.app",
        "io.github.jiangbyte.hei.trigger",
        "io.github.jiangbyte.hei.application",
        "io.github.jiangbyte.hei.infrastructure.persistence",
        "io.github.jiangbyte.hei.infrastructure.auth",
        "io.github.jiangbyte.hei.infrastructure.event",
        "io.github.jiangbyte.hei.infrastructure.tx"
})
public class HeiDddAiLiteApplication {

    public static void main(String[] args) {
        SpringApplication.run(HeiDddAiLiteApplication.class, args);
    }
}
