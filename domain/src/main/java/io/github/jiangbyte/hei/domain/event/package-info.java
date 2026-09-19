/**
 * 领域事件：描述聚合内已发生的事实。
 *
 * <p><b>职责</b>：解耦副作用（通知、投影、跨聚合协作）与聚合主流程。
 *
 * <p><b>放什么</b>：实现 {@code DomainEvent} 的不可变事件类型。
 * <b>不放什么</b>：事件监听器、消息中间件客户端（放 infrastructure）。
 *
 * <p><b>如何扩展</b>：聚合行为内 {@code registerEvent}；应用服务在 {@code save} 后
 * {@code pullDomainEvents} 并交给 {@code DomainEventPublisher}；消费在
 * {@code infrastructure.event}（如 {@code UserDomainEventListener}）。
 */
package io.github.jiangbyte.hei.domain.event;
