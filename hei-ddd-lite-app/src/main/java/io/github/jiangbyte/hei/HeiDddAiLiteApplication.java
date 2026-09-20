package io.github.jiangbyte.hei;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 应用启动入口。
 * <p>
 * 放在 {@code io.github.jiangbyte.hei} 根包，自动扫描 app / trigger / domain / application /
 * infrastructure 等子包。新增 BC（如 {@code domain.xxx}）无需再改扫描列表。
 * <p>
 * {@code infrastructure.config} 下的 {@code @AutoConfiguration} 由
 * {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports} 加载，
 * 并由 Spring Boot 自带的 {@code AutoConfigurationExcludeFilter} 排除出组件扫描，避免重复注册。
 */
@SpringBootApplication
public class HeiDddAiLiteApplication {

    public static void main(String[] args) {
        SpringApplication.run(HeiDddAiLiteApplication.class, args);
    }
}
