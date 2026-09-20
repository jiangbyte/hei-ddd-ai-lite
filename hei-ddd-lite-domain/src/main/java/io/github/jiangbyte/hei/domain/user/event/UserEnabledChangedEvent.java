package io.github.jiangbyte.hei.domain.user.event;

import io.github.jiangbyte.hei.domain.core.DomainEvent;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * 用户启用状态已变更事件。
 */
@Getter
public final class UserEnabledChangedEvent implements DomainEvent {

    private final Long userId;
    private final boolean enabled;
    private final OffsetDateTime occurredAt;

    public UserEnabledChangedEvent(Long userId, boolean enabled) {
        this.userId = Objects.requireNonNull(userId, "userId");
        this.enabled = enabled;
        this.occurredAt = OffsetDateTime.now();
    }

    @Override
    public OffsetDateTime occurredAt() {
        return occurredAt;
    }

    @Override
    public String aggregateId() {
        return String.valueOf(userId);
    }
}
