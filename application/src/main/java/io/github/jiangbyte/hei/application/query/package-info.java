/**
 * 读用例查询：只读入参，不应产生领域状态变更。
 *
 * <p><b>如何扩展</b>：每条查询一个 {@code Query} 实现类；复杂查询可读模型/投影，不必强行加载聚合。
 */
package io.github.jiangbyte.hei.application.query;
