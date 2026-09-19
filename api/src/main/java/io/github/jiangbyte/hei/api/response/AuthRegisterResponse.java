package io.github.jiangbyte.hei.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 注册成功返回（无 Token）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthRegisterResponse {

    private Long userId;
    private String username;
    private String userType;
}
