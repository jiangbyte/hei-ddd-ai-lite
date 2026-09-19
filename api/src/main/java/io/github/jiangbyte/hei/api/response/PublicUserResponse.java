package io.github.jiangbyte.hei.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 公开用户资料响应。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicUserResponse {

    private Long userId;
    private String username;
    private String userType;
}
