package io.github.jiangbyte.hei.infrastructure.config.s3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.net.URI;

/**
 * S3 兼容对象存储自动配置：注册 {@link S3Client} 与上传下载门面。
 * <p>
 * 依赖 AWS SDK for Java v2（s3），可通过 endpoint + path-style 对接 MinIO 等兼容实现。
 */
@AutoConfiguration
@ConditionalOnClass(S3Client.class)
@ConditionalOnProperty(prefix = "hei.ddd.s3", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(S3Properties.class)
public class S3AutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(S3AutoConfiguration.class);

    /**
     * 注册同步 S3Client；容器销毁时关闭底层连接。
     */
    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean
    public S3Client s3Client(S3Properties properties) {
        // 1. 静态 AK/SK + 区域（兼容端点也必须指定 region）
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(properties.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey())))
                .forcePathStyle(properties.isPathStyleAccess());

        // 2. 自定义 Endpoint（MinIO / R2 / 自建网关）；留空则走官方 AWS 端点
        if (properties.hasEndpoint()) {
            builder.endpointOverride(URI.create(properties.getEndpoint().trim()));
        }

        S3Client client = builder.build();
        log.info("S3Client 已创建: endpoint={}, region={}, pathStyle={}, bucket={}",
                properties.hasEndpoint() ? properties.getEndpoint() : "(aws-default)",
                properties.getRegion(),
                properties.isPathStyleAccess(),
                properties.getBucket());

        // 3. 可选初始化默认桶
        if (properties.isCreateBucketIfAbsent()) {
            ensureBucket(client, properties.getBucket());
        }
        return client;
    }

    /**
     * 注册对象存储门面。
     */
    @Bean
    @ConditionalOnMissingBean
    public S3ObjectStorage s3ObjectStorage(S3Client s3Client, S3Properties properties) {
        return new S3ObjectStorage(s3Client, properties);
    }

    private void ensureBucket(S3Client client, String bucket) {
        try {
            client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
        } catch (NoSuchBucketException e) {
            createBucketQuietly(client, bucket);
        } catch (S3Exception e) {
            // 部分兼容实现用 404/403 表达桶不存在，尝试创建
            if (e.statusCode() == 404 || e.statusCode() == 403) {
                createBucketQuietly(client, bucket);
            } else {
                log.warn("检查 S3 桶失败: bucket={}, status={}, msg={}", bucket, e.statusCode(), e.getMessage());
            }
        } catch (Exception e) {
            log.warn("检查 S3 桶失败: bucket={}, msg={}", bucket, e.getMessage());
        }
    }

    private void createBucketQuietly(S3Client client, String bucket) {
        try {
            client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
            log.info("已创建 S3 桶: {}", bucket);
        } catch (Exception e) {
            log.warn("创建 S3 桶失败: bucket={}, msg={}", bucket, e.getMessage());
        }
    }
}
