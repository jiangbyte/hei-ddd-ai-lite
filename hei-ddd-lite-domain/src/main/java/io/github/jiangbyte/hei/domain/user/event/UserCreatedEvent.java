package io.github.jiangbyte.hei.domain.user.event;

import io.github.jiangbyte.hei.domain.core.DomainEvent;
import io.github.jiangbyte.hei.domain.user.model.valobj.UserType;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * 用户聚合已创建事件。
 */
@Getter
public final class UserCreatedEvent implements DomainEvent {

    private final Long userId;
    private final String username;
    private final UserType userType;
    private final OffsetDateTime occurredAt;

    public UserCreatedEvent(Long userId, String username, UserType userType) {
        this.userId = Objects.requireNonNull(userId, "userId");
        this.username = Objects.requireNonNull(username, "username");
        this.userType = Objects.requireNonNull(userType, "userType");
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
