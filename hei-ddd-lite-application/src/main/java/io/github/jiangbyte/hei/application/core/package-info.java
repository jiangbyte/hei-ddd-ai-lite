/**
 * 应用层约定：ApplicationService / Command / Query 标记。
 *
 * <p><b>职责</b>：定义用例编排边界与入参形态，不承载领域规则本身。
 *
 * <p><b>放什么</b>：标记接口与通用应用约定。
 * <b>不放什么</b>：实体行为、HTTP DTO、持久化细节。
 *
 * <p><b>如何扩展</b>：写用例实现 {@code Command}/{@code Query}；应用服务实现
 * {@code ApplicationService}，用 {@code @Transactional} 划事务边界。
 */
package io.github.jiangbyte.hei.application.core;
