/**
 * 持久化适配：仓储实现、PO、Mapper。
 *
 * <p><b>放什么</b>：如 {@code UserRepositoryImpl}、{@code UserPo}、{@code UserMapper}。
 *
 * <p><b>不放什么</b>：领域规则、HTTP。
 *
 * <p><b>如何扩展</b>：实现 domain 仓储端口；用户账号示例已接 MySQL（sys_user）。
 */
package io.github.jiangbyte.hei.infrastructure.persistence;
