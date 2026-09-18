package io.github.jiangbyte.hei.domain.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 聚合根基类：聚合一致性边界入口，维护领域事件登记，由应用服务在保存后统一拉取并发布。
 *
 * @param <ID> 聚合根标识类型
 */
public abstract class AggregateRoot<ID extends Serializable> extends Entity<ID> {

    private static final long serialVersionUID = 1L;

    private final transient List<DomainEvent> domainEvents = new ArrayList<>();

    /**
     * 登记领域事件，供事务提交前由应用服务拉取发布。
     */
    protected void registerEvent(DomainEvent event) {
        if (event != null) {
            domainEvents.add(event);
        }
    }

    /**
     * 取出并清空已登记事件（发布侧消费，避免重复派发）。
     */
    public List<DomainEvent> pullDomainEvents() {
        // 1. 复制当前事件列表
        List<DomainEvent> events = new ArrayList<>(domainEvents);
        // 2. 清空内部登记，避免重复发布
        domainEvents.clear();
        // 3. 返回不可变视图，防止外部篡改
        return Collections.unmodifiableList(events);
    }

    /**
     * 查看尚未发布的事件（只读）。
     */
    public List<DomainEvent> peekDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }
}
