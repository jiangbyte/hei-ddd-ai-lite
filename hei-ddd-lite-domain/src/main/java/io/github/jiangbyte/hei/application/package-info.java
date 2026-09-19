/**
 * 用例编排：事务边界、Command/Query、应用服务。
 *
 * <p><b>说明</b>：按常见六层工程模型，用例编排放在 domain 模块的 {@code application} 包，
 * 不单独拆 Maven 模块。
 *
 * <p><b>如何扩展</b>：参考 {@code AuthApplicationService} /
 * {@code AdminUserApplicationService}——接收 Command → 工厂/聚合/领域服务 →
 * 仓储保存 → {@code pullDomainEvents} 发布；无状态领域服务见
 * {@code UserDomainConfiguration}。
 */
package io.github.jiangbyte.hei.application;
