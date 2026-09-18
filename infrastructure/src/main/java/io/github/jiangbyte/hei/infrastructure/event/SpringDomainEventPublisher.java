package io.github.jiangbyte.hei.infrastructure.event;

import io.github.jiangbyte.hei.domain.core.DomainEvent;
import io.github.jiangbyte.hei.domain.core.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 基于 Spring ApplicationEventPublisher 的同步领域事件发布实现。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SpringDomainEventPublisher implements DomainEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(DomainEvent event) {
        if (event == null) {
            return;
        }
        log.debug("发布领域事件: type={}, aggregateId={}", event.getClass().getSimpleName(), event.aggregateId());
        applicationEventPublisher.publishEvent(event);
    }
}
