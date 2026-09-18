/**
 * 应用层：用例编排入口与 Command / Query / 读模型。
 *
 * <p><b>职责</b>：协调工厂、仓储、领域事件发布；定义事务边界。
 *
 * <p><b>放什么</b>：{@code *ApplicationService}、command/query/dto。
 * <b>不放什么</b>：领域不变式、Controller、Mapper。
 *
 * <p><b>如何扩展</b>：复制 {@code HelloApplicationService}——接收 Command → 工厂/聚合 →
 * {@code repository.save} → {@code pullDomainEvents} → {@code DomainEventPublisher.publish}。
 */
package io.github.jiangbyte.hei.application;
