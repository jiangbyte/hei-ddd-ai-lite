package io.github.jiangbyte.hei.domain.event;

import io.github.jiangbyte.hei.domain.core.DomainEvent;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Hello 问候语已变更事件。
 */
@Getter
public final class HelloGreetingChangedEvent implements DomainEvent {

    private final Long helloId;
    private final String greetingText;
    private final OffsetDateTime occurredAt;

    public HelloGreetingChangedEvent(Long helloId, String greetingText) {
        this.helloId = Objects.requireNonNull(helloId, "helloId");
        this.greetingText = greetingText;
        this.occurredAt = OffsetDateTime.now();
    }

    @Override
    public OffsetDateTime occurredAt() {
        return occurredAt;
    }

    @Override
    public String aggregateId() {
        return String.valueOf(helloId);
    }
}
