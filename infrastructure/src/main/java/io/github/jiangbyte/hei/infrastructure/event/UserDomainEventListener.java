package io.github.jiangbyte.hei.infrastructure.event;

import io.github.jiangbyte.hei.domain.event.UserCreatedEvent;
import io.github.jiangbyte.hei.domain.event.UserEnabledChangedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 用户领域事件消费示例：闭合「登记 → pull → publish → listen」。
 * <p>
 * 教学向实现仅打结构化日志；可替换为通知、审计或投递 MQ。
 */
@Slf4j
@Component
public class UserDomainEventListener {

    /**
     * 消费用户创建事件。
     */
    @EventListener
    public void onUserCreated(UserCreatedEvent event) {
        // 1. 空事件直接忽略，避免 NPE
        if (event == null) {
            return;
        }
        // 2. 记录创建事实，便于对照发布链路与排查
        log.info("领域事件已消费: type=UserCreatedEvent, userId={}, username={}, userType={}, occurredAt={}",
                event.getUserId(), event.getUsername(), event.getUserType(), event.occurredAt());
    }

    /**
     * 消费用户启停变更事件。
     */
    @EventListener
    public void onUserEnabledChanged(UserEnabledChangedEvent event) {
        // 1. 空事件直接忽略
        if (event == null) {
            return;
        }
        // 2. 记录启停变更，与聚合 changeEnabled 登记对应
        log.info("领域事件已消费: type=UserEnabledChangedEvent, userId={}, enabled={}, occurredAt={}",
                event.getUserId(), event.isEnabled(), event.occurredAt());
    }
}
