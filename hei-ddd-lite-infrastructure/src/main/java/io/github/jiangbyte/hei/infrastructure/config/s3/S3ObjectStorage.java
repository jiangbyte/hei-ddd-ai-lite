package io.github.jiangbyte.hei.infrastructure.config.s3;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;

/**
 * S3 对象存储门面：封装常见上传/下载/删除（协议通用，不绑定某一厂商）。
 */
@RequiredArgsConstructor
public class S3ObjectStorage {

    private final S3Client s3Client;
    private final S3Properties properties;

    /**
     * 上传对象到默认桶。
     */
    public void upload(String objectKey, InputStream stream, long size, String contentType) {
        // 1. 组装 PutObject 请求（桶、Key、Content-Type）
        PutObjectRequest.Builder request = PutObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(objectKey);
        if (contentType != null && !contentType.isBlank()) {
            request.contentType(contentType);
        }
        // 2. 按已知长度写入对象流
        s3Client.putObject(request.build(), RequestBody.fromInputStream(stream, size));
    }

    /**
     * 从默认桶下载对象。
     */
    public InputStream download(String objectKey) {
        return s3Client.getObject(
                GetObjectRequest.builder()
                        .bucket(properties.getBucket())
                        .key(objectKey)
                        .build(),
                ResponseTransformer.toInputStream());
    }

    /**
     * 删除默认桶中的对象。
     */
    public void remove(String objectKey) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(objectKey)
                .build());
    }

    /**
     * 默认桶名。
     */
    public String bucket() {
        return properties.getBucket();
    }
}
