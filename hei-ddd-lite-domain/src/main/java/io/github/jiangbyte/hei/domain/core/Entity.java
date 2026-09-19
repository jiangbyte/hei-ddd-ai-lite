package io.github.jiangbyte.hei.domain.core;

import java.io.Serializable;
import java.util.Objects;

/**
 * 领域实体基类：有唯一标识、生命周期内可变，按 ID 判定相等。
 *
 * @param <ID> 实体标识类型
 */
public abstract class Entity<ID extends Serializable> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 获取实体唯一标识。
     */
    public abstract ID getId();

    @Override
    public boolean equals(Object o) {
        // 1. 同引用直接相等
        if (this == o) {
            return true;
        }
        // 2. 类型不一致则不等
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        // 3. 双方均有 ID 时按 ID 比较；任一 ID 为空则仅同引用才相等
        Entity<?> entity = (Entity<?>) o;
        return getId() != null && Objects.equals(getId(), entity.getId());
    }

    @Override
    public int hashCode() {
        return getId() == null ? System.identityHashCode(this) : Objects.hash(getId());
    }
}
