/**
 * 持久化适配：实现领域仓储端口。
 *
 * <p><b>放什么</b>：{@code *RepositoryImpl}、后续 Mapper/PO 映射。
 * <b>不放什么</b>：领域规则、Controller。
 *
 * <p><b>如何扩展</b>：Hello 仍为内存仓储；用户账号已接 MySQL（sys_user）。
 */
package io.github.jiangbyte.hei.infrastructure.persistence;
