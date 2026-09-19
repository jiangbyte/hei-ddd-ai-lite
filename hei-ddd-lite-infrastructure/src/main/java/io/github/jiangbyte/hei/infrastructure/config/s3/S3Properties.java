package io.github.jiangbyte.hei.infrastructure.config.s3;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

/**
 * S3 兼容对象存储配置（AWS S3 / MinIO / R2 / OSS S3 网关等）。
 */
@Data
@ConfigurationProperties(prefix = "hei.ddd.s3")
public class S3Properties {

    /** 是否启用并注册 S3Client */
    private boolean enabled = true;

    /**
     * 自定义 Endpoint。留空则走官方 AWS 区域端点；
     * MinIO / 自建网关示例：http://127.0.0.1:9000
     */
    private String endpoint;

    /** 区域；自建兼容端点也需填写（常用 us-east-1） */
    private String region = "us-east-1";

    /** Access Key / AK */
    private String accessKey = "admin";

    /** Secret Key / SK */
    private String secretKey = "infra123!";

    /** 默认桶名 */
    private String bucket = "hei";

    /** 是否强制 path-style（MinIO 等兼容端点一般需要 true） */
    private boolean pathStyleAccess = true;

    /** 启动时是否尝试创建默认桶 */
    private boolean createBucketIfAbsent = false;

    /**
     * 是否配置了自定义 Endpoint。
     */
    public boolean hasEndpoint() {
        return StringUtils.hasText(endpoint);
    }
}
