/**
 * 领域模型：聚合根、实体、值对象的具体业务类型。
 *
 * <p><b>职责</b>：承载业务状态与不变式，表达领域语言。
 *
 * <p><b>放什么</b>：如 {@code Hello}、{@code Greeting}。
 * <b>不放什么</b>：Controller、DTO、Mapper、Spring 注解。
 *
 * <p><b>如何扩展</b>：复制 {@code Hello} 竖切——新建聚合继承 AggregateRoot，
 * 状态变更走行为方法并登记领域事件；值对象用工厂方法保证合法。
 */
package io.github.jiangbyte.hei.domain.model;
