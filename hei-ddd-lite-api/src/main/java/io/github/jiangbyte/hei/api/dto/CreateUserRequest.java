package io.github.jiangbyte.hei.api.dto;

import lombok.Data;

/**
 * 后台创建用户请求。
 */
@Data
public class CreateUserRequest {

    private String username;
    private String password;
    /** PORTAL / ADMIN，默认 PORTAL */
    private String userType;
}
