/**
 * 触发器层：入口适配（HTTP / 鉴权 / 全局异常），实现 api 契约。
 *
 * <p><b>放什么</b>：Controller（implements {@code I*Service}）、Assembler、JWT。
 *
 * <p><b>不放什么</b>：对外 DTO（在 api）、领域逻辑、直接访问 Mapper。
 *
 * <p><b>如何扩展</b>：复制 {@code AdminUserController} /
 * {@code AuthController}——实现 api 接口，组装 Command/Query，委托 domain 内用例服务。
 */
package io.github.jiangbyte.hei.trigger.web;
