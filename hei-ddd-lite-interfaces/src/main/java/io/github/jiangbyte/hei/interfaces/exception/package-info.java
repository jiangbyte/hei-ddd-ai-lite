/**
 * 接口层异常映射：将领域/业务/校验异常转为统一 {@code R}。
 *
 * <p><b>如何扩展</b>：新增异常类型时在 {@code GlobalExceptionHandler} 增加映射，勿向客户端泄露内部细节。
 */
package io.github.jiangbyte.hei.interfaces.exception;
