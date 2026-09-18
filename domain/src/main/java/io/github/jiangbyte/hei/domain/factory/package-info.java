/**
 * 领域工厂：创建合法聚合，集中构造与创建事件登记。
 *
 * <p><b>职责</b>：避免应用服务直接 {@code new} 出不完整聚合。
 *
 * <p><b>放什么</b>：实现 {@code Factory} 的创建逻辑。
 * <b>不放什么</b>：持久化、事务、HTTP。
 *
 * <p><b>如何扩展</b>：参考 {@code HelloFactory}，先校验/规约，再调用聚合静态工厂。
 */
package io.github.jiangbyte.hei.domain.factory;
