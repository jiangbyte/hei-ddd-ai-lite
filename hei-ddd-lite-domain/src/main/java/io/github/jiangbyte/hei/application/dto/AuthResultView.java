package io.github.jiangbyte.hei.application.dto;

import io.github.jiangbyte.hei.domain.model.UserType;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 注册/登录业务结果（不含 Token）。
 */
@Getter
@AllArgsConstructor
public class AuthResultView {

    private final Long userId;
    private final String username;
    private final UserType userType;
}
