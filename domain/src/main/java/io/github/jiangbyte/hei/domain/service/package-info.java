/**
 * 领域服务：不属于单一聚合的无状态领域规则。
 *
 * <p><b>放什么</b>：实现 {@code DomainService} 的纯领域逻辑。
 *
 * <p><b>不放什么</b>：事务、仓储 IO、HTTP、技术适配。
 *
 * <p><b>如何扩展</b>：参考 {@code UserClientAccessPolicy}；由 application 侧 {@code @Bean} 装配。
 */
package io.github.jiangbyte.hei.domain.service;
