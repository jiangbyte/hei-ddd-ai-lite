/**
 * 领域模型：聚合根、实体与值对象。
 *
 * <p><b>放什么</b>：如 {@code User}、{@code Username}、{@code UserType}。
 *
 * <p><b>不放什么</b>：仓储实现、应用编排、HTTP DTO。
 *
 * <p><b>如何扩展</b>：参考 {@code User} 竖切——新建聚合继承 AggregateRoot，
 * 值对象实现 ValueObject，行为内 {@code registerEvent}。
 */
package io.github.jiangbyte.hei.domain.model;
