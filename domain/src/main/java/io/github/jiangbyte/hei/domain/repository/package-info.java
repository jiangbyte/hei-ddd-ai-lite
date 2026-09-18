/**
 * 领域仓储端口：按聚合定义持久化契约。
 *
 * <p><b>职责</b>：依赖倒置——领域定义接口，基础设施实现。
 *
 * <p><b>放什么</b>：如 {@code HelloRepository}。
 * <b>不放什么</b>：SQL、MyBatis Mapper、内存 Map 实现。
 *
 * <p><b>如何扩展</b>：为新聚合新建 {@code XxxRepository extends Repository<Xxx, ID>}，
 * 实现类放 {@code infrastructure.persistence}。
 */
package io.github.jiangbyte.hei.domain.repository;
