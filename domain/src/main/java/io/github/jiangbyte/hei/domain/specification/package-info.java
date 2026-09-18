/**
 * 领域规约：可组合的业务判定条件。
 *
 * <p><b>职责</b>：把「是否满足某业务条件」从方法内 if 抽成可复用对象。
 *
 * <p><b>放什么</b>：实现 {@code Specification} 的判定类。
 * <b>不放什么</b>：副作用、IO、事务。
 *
 * <p><b>如何扩展</b>：参考 {@code GreetingNotBlankSpecification}；复杂条件可组合多个规约。
 */
package io.github.jiangbyte.hei.domain.specification;
