package io.github.jiangbyte.hei.infrastructure.config.milvus;

import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

/**
 * Milvus 自动配置：注册 MilvusClientV2 与常用门面。
 * <p>
 * 需引入 milvus-sdk-java，并通过 hei.ddd.milvus.enabled=true 启用。
 */
@AutoConfiguration
@ConditionalOnClass(MilvusClientV2.class)
@ConditionalOnProperty(prefix = "hei.ddd.milvus", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(MilvusProperties.class)
public class MilvusAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(MilvusAutoConfiguration.class);

    /**
     * 注册 Milvus Java SDK v2 客户端，容器销毁时关闭连接。
     */
    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean
    public MilvusClientV2 milvusClientV2(MilvusProperties properties) {
        // 1. 组装 ConnectConfig（URI / 鉴权 / 库名 / 超时）
        ConnectConfig.ConnectConfigBuilder builder = ConnectConfig.builder()
                .uri(properties.resolveUri())
                .dbName(properties.getDatabase())
                .connectTimeoutMs(properties.getConnectTimeoutMs());
        if (StringUtils.hasText(properties.getToken())) {
            builder.token(properties.getToken().trim());
        } else if (StringUtils.hasText(properties.getUsername())) {
            builder.username(properties.getUsername().trim());
            if (StringUtils.hasText(properties.getPassword())) {
                builder.password(properties.getPassword());
            }
        }

        // 2. 创建客户端
        MilvusClientV2 client = new MilvusClientV2(builder.build());
        log.info("Milvus 客户端已创建: uri={}, database={}", properties.resolveUri(), properties.getDatabase());

        // 3. 可选探活：失败仅告警，方便本地先起应用再起向量库
        if (properties.isPingOnStartup()) {
            pingQuietly(client);
        }
        return client;
    }

    /**
     * 注册常用操作门面。
     */
    @Bean
    @ConditionalOnMissingBean
    public MilvusOperations milvusOperations(MilvusClientV2 milvusClientV2, MilvusProperties properties) {
        return new MilvusOperations(milvusClientV2, properties);
    }

    private void pingQuietly(MilvusClientV2 client) {
        try {
            var databases = client.listDatabases();
            int size = databases == null || databases.getDatabaseNames() == null
                    ? 0
                    : databases.getDatabaseNames().size();
            log.info("Milvus 探活成功，可见数据库数={}", size);
        } catch (Exception e) {
            log.warn("Milvus 探活失败（客户端已注册，请确认服务可用）: {}", e.getMessage());
        }
    }
}
