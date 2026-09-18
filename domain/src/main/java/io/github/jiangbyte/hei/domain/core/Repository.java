package io.github.jiangbyte.hei.domain.core;

import java.io.Serializable;
import java.util.Optional;

/**
 * 仓储端口约定：以聚合为粒度，由领域层定义、基础设施层实现。
 *
 * @param <T>  聚合类型
 * @param <ID> 标识类型
 */
public interface Repository<T, ID extends Serializable> {

    /**
     * 保存（新增或更新）聚合。
     */
    T save(T aggregate);

    /**
     * 按标识加载聚合。
     */
    Optional<T> findById(ID id);

    /**
     * 按标识删除聚合。
     */
    void remove(ID id);
}
