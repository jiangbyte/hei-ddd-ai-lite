package io.github.jiangbyte.hei.trigger.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 需要后台管理员登录。隐含登录校验，且 userType 必须为 ADMIN。
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RequireLogin
public @interface RequireAdmin {
}
