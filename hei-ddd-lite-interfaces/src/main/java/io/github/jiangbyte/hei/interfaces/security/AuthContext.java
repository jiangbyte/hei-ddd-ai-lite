package io.github.jiangbyte.hei.interfaces.security;

/**
 * 登录用户上下文：基于 ThreadLocal 在一次请求内传递当前用户。
 */
public final class AuthContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private AuthContext() {
    }

    /**
     * 设置当前登录用户。
     */
    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    /**
     * 获取当前登录用户，未登录时返回 null。
     */
    public static LoginUser get() {
        return HOLDER.get();
    }

    /**
     * 获取当前用户 ID，未登录时返回 null。
     */
    public static String getUserId() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.getUserId();
    }

    /**
     * 清理上下文，防止线程复用泄漏。
     */
    public static void clear() {
        HOLDER.remove();
    }
}
