package io.github.jiangbyte.hei.infrastructure.persistence;

import io.github.jiangbyte.hei.domain.model.Hello;
import io.github.jiangbyte.hei.domain.repository.HelloRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Hello 内存仓储实现：脚手架默认开箱可跑，接真库时替换为本实现。
 */
@Repository
public class HelloRepositoryImpl implements HelloRepository {

    private final Map<Long, Hello> store = new ConcurrentHashMap<>();

    @Override
    public Hello save(Hello aggregate) {
        // 1. 以还原实例写入存储，隔离外部引用；不携带待发布事件
        store.put(aggregate.getId(), Hello.restore(aggregate.getId(), aggregate.getGreeting()));
        // 2. 返回原聚合，保留待发布领域事件供应用服务拉取
        return aggregate;
    }

    @Override
    public Optional<Hello> findById(Long id) {
        // 1. 查存储；不存在返回 empty
        Hello stored = store.get(id);
        if (stored == null) {
            return Optional.empty();
        }
        // 2. 返回还原实例，避免外部改动污染存储
        return Optional.of(Hello.restore(stored.getId(), stored.getGreeting()));
    }

    @Override
    public void remove(Long id) {
        store.remove(id);
    }
}
