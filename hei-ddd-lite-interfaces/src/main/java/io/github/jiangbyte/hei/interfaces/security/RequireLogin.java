package io.github.jiangbyte.hei.interfaces.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 登录校验注解：标注在 Controller 类或方法上，表示该接口需要登录后访问。
 * <p>
 * 由 {@link LoginAuthInterceptor} 解析 Authorization Bearer Token（JWT）完成校验。
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireLogin {
}
