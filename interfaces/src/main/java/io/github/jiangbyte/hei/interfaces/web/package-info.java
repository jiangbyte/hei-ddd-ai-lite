/**
 * Web 适配：Controller 接收 HTTP，调用应用服务，返回统一响应。
 *
 * <p><b>放什么</b>：RestController。
 * <b>不放什么</b>：领域规则、SQL、事务编排（事务在应用服务）。
 *
 * <p><b>如何扩展</b>：复制 {@code HelloController}——组装 Command/Query，委托应用服务，经 Assembler 输出。
 */
package io.github.jiangbyte.hei.interfaces.web;
