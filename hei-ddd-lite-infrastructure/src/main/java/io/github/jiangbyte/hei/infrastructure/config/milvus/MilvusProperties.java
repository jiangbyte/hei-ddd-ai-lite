package io.github.jiangbyte.hei.infrastructure.config.milvus;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

/**
 * Milvus 连接配置属性。
 */
@Data
@ConfigurationProperties(prefix = "hei.ddd.milvus")
public class MilvusProperties {

    /** 是否启用并注册 MilvusClientV2 */
    private boolean enabled = false;

    /** 服务 URI，例如 http://127.0.0.1:19530 */
    private String uri = "http://127.0.0.1:19530";

    /** 可选：host（未配 uri 时拼装用） */
    private String host = "127.0.0.1";

    /** 可选：port（未配 uri 时拼装用） */
    private int port = 19530;

    /** 数据库名，默认 default */
    private String database = "default";

    /** Token 鉴权（与 username/password 二选一，优先 token） */
    private String token;

    /** 用户名（本地无鉴权可留空） */
    private String username;

    /** 密码 */
    private String password;

    /** 连接超时（毫秒） */
    private long connectTimeoutMs = 10000L;

    /** 启动时是否探活（listDatabases）；失败仅打日志不阻断启动 */
    private boolean pingOnStartup = true;

    /**
     * 解析最终连接 URI。
     */
    public String resolveUri() {
        if (StringUtils.hasText(uri)) {
            return uri.trim();
        }
        return "http://" + host + ":" + port;
    }
}
