/**
 * 领域仓储端口：以聚合为粒度的持久化抽象。
 *
 * <p><b>放什么</b>：如 {@code UserRepository}。
 *
 * <p><b>不放什么</b>：SQL、MyBatis Mapper、PO。
 *
 * <p><b>如何扩展</b>：{@code XxxRepository extends Repository<Xxx, ID>}，实现放在 infrastructure。
 */
package io.github.jiangbyte.hei.domain.repository;
