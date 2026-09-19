package io.github.jiangbyte.hei.api.dto;

import lombok.Data;

/**
 * 登录 / 注册请求体。
 */
@Data
public class AuthRequest {

    private String username;
    private String password;
    /**
     * 登录端：PORTAL（默认）或 ADMIN。注册忽略，固定为前台用户。
     */
    private String clientType;
}
