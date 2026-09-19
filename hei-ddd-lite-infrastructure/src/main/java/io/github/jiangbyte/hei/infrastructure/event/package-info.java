/**
 * 领域事件基础设施：发布与消费。
 *
 * <p><b>放什么</b>：{@code DomainEventPublisher} 实现、{@code @EventListener} 消费（如
 * {@code SpringDomainEventPublisher}、{@code UserDomainEventListener}）。
 *
 * <p><b>如何扩展</b>：可改为 Outbox / MQ；应用服务调用方式不变。
 */
package io.github.jiangbyte.hei.infrastructure.event;
