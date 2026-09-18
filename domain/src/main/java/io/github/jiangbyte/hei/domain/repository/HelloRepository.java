package io.github.jiangbyte.hei.domain.repository;

import io.github.jiangbyte.hei.domain.core.Repository;
import io.github.jiangbyte.hei.domain.model.Hello;

/**
 * Hello 领域仓储端口：由基础设施层实现。
 */
public interface HelloRepository extends Repository<Hello, Long> {
}
