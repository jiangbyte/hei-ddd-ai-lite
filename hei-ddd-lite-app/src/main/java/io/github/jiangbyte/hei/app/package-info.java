/**
 * 启动模块（app）：Spring Boot 入口与主配置。
 *
 * <p><b>职责</b>：组装 trigger + infrastructure，扫描业务组件包。
 *
 * <p><b>如何扩展</b>：新增 infrastructure 下需扫描的包时，同步更新
 * {@code HeiDddAiLiteApplication#scanBasePackages}；可选 AutoConfiguration 仍走 imports。
 */
package io.github.jiangbyte.hei.app;
