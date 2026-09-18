/**
 * 领域内核：Entity / ValueObject / AggregateRoot / DomainEvent / Repository /
 * DomainService / Factory / Specification / DomainEventPublisher 等脚手架约定。
 *
 * <p><b>职责</b>：定义与技术无关的领域基础抽象，不含 Spring / HTTP / SQL。
 *
 * <p><b>放什么</b>：可复用的基类、标记接口、领域异常。
 * <b>不放什么</b>：具体业务聚合、仓储实现、框架注解。
 *
 * <p><b>如何扩展</b>：
 * <ul>
 *   <li>新聚合根继承 {@link io.github.jiangbyte.hei.domain.core.AggregateRoot}，在行为方法内
 *       {@code registerEvent}，由应用服务保存后 {@code pullDomainEvents} + 发布；</li>
 *   <li>值对象实现 {@link io.github.jiangbyte.hei.domain.core.ValueObject}，保持不可变；</li>
 *   <li>仓储端口继承 {@link io.github.jiangbyte.hei.domain.core.Repository}，实现落在 infrastructure；</li>
 *   <li>跨聚合规则用 {@link io.github.jiangbyte.hei.domain.core.DomainService}；
 *       复杂创建用 {@link io.github.jiangbyte.hei.domain.core.Factory}；可复用判定用
 *       {@link io.github.jiangbyte.hei.domain.core.Specification}。</li>
 * </ul>
 */
package io.github.jiangbyte.hei.domain.core;
