package io.github.jiangbyte.hei.interfaces.security;

import io.github.jiangbyte.hei.domain.core.BizException;

/**
 * 未登录 / Token 无效异常。
 */
public class UnauthorizedException extends BizException {

    private static final long serialVersionUID = 1L;

    public UnauthorizedException(String message) {
        super("UNAUTHORIZED", message);
    }
}
