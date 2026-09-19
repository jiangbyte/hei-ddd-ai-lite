package io.github.jiangbyte.hei.domain.model;

/**
 * 用户端类型：前台用户 / 后台管理员（非 RBAC，仅区分登录端）。
 */
public enum UserType {
    PORTAL,
    ADMIN;

    public static UserType from(String value) {
        if (value == null || value.isBlank()) {
            return PORTAL;
        }
        return UserType.valueOf(value.trim().toUpperCase());
    }
}
