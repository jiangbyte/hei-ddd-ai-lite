package io.github.jiangbyte.hei.application.dto;

import io.github.jiangbyte.hei.domain.model.UserType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 当前用户可见的账号信息（不含密码）。
 */
@Getter
@AllArgsConstructor
public class UserProfileView {

    private final Long userId;
    private final String username;
    private final UserType userType;
    private final boolean enabled;
    private final LocalDateTime createTime;
    private final LocalDateTime updateTime;
}
