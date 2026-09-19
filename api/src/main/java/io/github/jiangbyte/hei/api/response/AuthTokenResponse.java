package io.github.jiangbyte.hei.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录成功返回（含 Token）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthTokenResponse {

    private Long userId;
    private String username;
    private String userType;
    private String token;
    private String tokenType;
    private long expiresIn;
}
