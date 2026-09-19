/**
 * 领域规约：可复用的业务判定。
 *
 * <p><b>放什么</b>：实现 {@code Specification} 的判定类。
 *
 * <p><b>不放什么</b>：带 I/O 的查询、应用层校验文案拼装。
 *
 * <p><b>如何扩展</b>：参考 {@code UsernameFormatSpecification}；复杂条件可组合多个规约。
 */
package io.github.jiangbyte.hei.domain.specification;
