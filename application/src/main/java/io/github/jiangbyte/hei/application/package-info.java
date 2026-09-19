/**
 * 应用层：用例编排与事务边界。
 *
 * <p><b>放什么</b>：ApplicationService、Command、Query、读模型 DTO、领域服务 {@code @Bean} 装配。
 *
 * <p><b>不放什么</b>：领域不变式细节、HTTP 注解、SQL。
 *
 * <p><b>如何扩展</b>：参考 {@code AuthApplicationService} /
 * {@code AdminUserApplicationService}——接收 Command → 工厂/聚合/领域服务 →
 * 仓储保存 → {@code pullDomainEvents} 发布；无 Spring 的领域服务见
 * {@code UserDomainConfiguration}。
 */
package io.github.jiangbyte.hei.application;
