package io.github.jiangbyte.hei.interfaces.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

/**
 * 当前登录用户信息（从 JWT 解析后放入请求上下文）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userId;
    private String username;
    /** PORTAL / ADMIN */
    private String userType;
    private String jti;
    private Instant expireAt;

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(userType);
    }
}
