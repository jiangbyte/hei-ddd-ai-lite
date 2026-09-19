package io.github.jiangbyte.hei.trigger.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * CORS 跨域配置属性。
 */
@Data
@ConfigurationProperties(prefix = "hei.ddd.cors")
public class CorsProperties {

    /** 是否启用 CORS */
    private boolean enabled = true;

    /** 允许的来源，支持 * 或具体域名 */
    private List<String> allowedOrigins = new ArrayList<>(List.of("*"));

    /** 允许的请求头 */
    private List<String> allowedHeaders = new ArrayList<>(List.of("*"));

    /** 允许的方法 */
    private List<String> allowedMethods = new ArrayList<>(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

    /** 是否允许携带 Cookie */
    private boolean allowCredentials = false;

    /** 预检缓存时间（秒） */
    private long maxAge = 3600;
}
