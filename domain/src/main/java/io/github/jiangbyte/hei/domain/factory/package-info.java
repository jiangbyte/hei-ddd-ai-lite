/**
 * 领域工厂：保证聚合创建时的不变式。
 *
 * <p><b>放什么</b>：实现 {@code Factory} 的创建逻辑。
 *
 * <p><b>不放什么</b>：持久化、事务、HTTP。
 *
 * <p><b>如何扩展</b>：参考 {@code UserFactory}，先值对象/规约校验，再调用聚合静态工厂。
 */
package io.github.jiangbyte.hei.domain.factory;
