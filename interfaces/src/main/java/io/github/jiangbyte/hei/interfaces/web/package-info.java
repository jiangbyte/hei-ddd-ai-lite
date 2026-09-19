/**
 * Web 接口：HTTP 适配，实现 api 契约，不写业务规则。
 *
 * <p><b>放什么</b>：Controller（implements {@code I*Service}）、Assembler、JWT。
 *
 * <p><b>不放什么</b>：对外 DTO（在 api 模块）、领域逻辑、直接访问 Mapper。
 *
 * <p><b>如何扩展</b>：复制 {@code AdminUserController} /
 * {@code AuthController}——实现 api 接口，组装 Command/Query，委托应用服务，经 Assembler 输出。
 */
package io.github.jiangbyte.hei.interfaces.web;
