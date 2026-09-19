package io.github.jiangbyte.hei.domain.core;

import java.time.OffsetDateTime;

/**
 * 领域事件：描述聚合内已发生的事实，由聚合根登记、应用服务在持久化后发布。
 */
public interface DomainEvent {

    /**
     * 事件发生时间。
     */
    OffsetDateTime occurredAt();

    /**
     * 产生事件的聚合标识（字符串形态，便于跨边界传递）。
     */
    String aggregateId();
}
