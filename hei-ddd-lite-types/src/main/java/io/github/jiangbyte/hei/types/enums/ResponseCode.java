package io.github.jiangbyte.hei.types.enums;

/**
 * 通用业务响应码约定（字符串，与统一包装 {@code R.code} 对齐）。
 */
public final class ResponseCode {

    private ResponseCode() {
    }

    /** 成功 */
    public static final String SUCCESS = "0";

    /** 参数校验失败 */
    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";

    /** 未登录 / Token 无效 */
    public static final String UNAUTHORIZED = "UNAUTHORIZED";

    /** 无权限 */
    public static final String FORBIDDEN = "FORBIDDEN";

    /** 登录端类型不匹配 */
    public static final String FORBIDDEN_CLIENT = "FORBIDDEN_CLIENT";

    /** 用户名或密码错误 */
    public static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";

    /** 用户名已占用 */
    public static final String USERNAME_TAKEN = "USERNAME_TAKEN";

    /** 用户不存在 */
    public static final String USER_NOT_FOUND = "USER_NOT_FOUND";

    /** 领域规则违反（未映射为具体业务码时） */
    public static final String DOMAIN_ERROR = "DOMAIN_ERROR";

    /** 系统异常 */
    public static final String SYSTEM_ERROR = "SYSTEM_ERROR";
}
