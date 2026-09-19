/**
 * 领域内核：Entity / ValueObject / AggregateRoot / DomainEvent / Repository /
 * DomainService / Factory / Specification / DomainEventPublisher 等脚手架约定。
 *
 * <p><b>职责</b>：领域基础抽象；用例编排类在同模块的 {@code application} 包中。
 *
 * <p><b>放什么</b>：可复用的基类、标记接口、领域异常。
 * <b>不放什么</b>：HTTP、SQL、对外 API DTO。
 */
package io.github.jiangbyte.hei.domain.core;
