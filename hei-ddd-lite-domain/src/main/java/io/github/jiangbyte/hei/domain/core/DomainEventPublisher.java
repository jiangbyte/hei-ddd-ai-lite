package io.github.jiangbyte.hei.domain.core;

import java.util.List;

/**
 * 领域事件发布端口：由基础设施实现（进程内总线 / Outbox 等）。
 */
public interface DomainEventPublisher {

    /**
     * 发布单个领域事件。
     */
    void publish(DomainEvent event);

    /**
     * 批量发布领域事件。
     */
    default void publish(List<? extends DomainEvent> events) {
        // 1. 空集合直接返回，避免无意义遍历
        if (events == null || events.isEmpty()) {
            return;
        }
        // 2. 逐条发布，保持顺序与聚合登记一致
        for (DomainEvent event : events) {
            if (event != null) {
                publish(event);
            }
        }
    }
}
