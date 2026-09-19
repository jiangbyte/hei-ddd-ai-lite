package io.github.jiangbyte.hei.infrastructure.config.milvus;

import io.milvus.v2.client.MilvusClientV2;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * Milvus 门面：封装常用探活与集合查询，业务侧可直接注入 MilvusClientV2。
 */
@RequiredArgsConstructor
public class MilvusOperations {

    private final MilvusClientV2 milvusClient;
    private final MilvusProperties properties;

    /**
     * 暴露底层客户端，便于高级操作。
     */
    public MilvusClientV2 client() {
        return milvusClient;
    }

    /**
     * 当前配置的数据库名。
     */
    public String database() {
        return properties.getDatabase();
    }

    /**
     * 列出当前连接可见的数据库。
     */
    public List<String> listDatabases() {
        var resp = milvusClient.listDatabases();
        if (resp == null || resp.getDatabaseNames() == null) {
            return Collections.emptyList();
        }
        return resp.getDatabaseNames();
    }

    /**
     * 列出当前数据库下的集合名。
     */
    public List<String> listCollections() {
        var resp = milvusClient.listCollections();
        if (resp == null || resp.getCollectionNames() == null) {
            return Collections.emptyList();
        }
        return resp.getCollectionNames();
    }
}
