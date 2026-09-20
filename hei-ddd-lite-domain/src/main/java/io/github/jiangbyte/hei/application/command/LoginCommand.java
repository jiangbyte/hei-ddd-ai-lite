package io.github.jiangbyte.hei.application.command;

import io.github.jiangbyte.hei.application.core.Command;
import io.github.jiangbyte.hei.domain.user.model.valobj.UserType;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 登录写用例入参（含期望端类型）。
 */
@Getter
@AllArgsConstructor
public class LoginCommand implements Command {

    private final String username;
    private final String password;
    /** 期望登录端：PORTAL / ADMIN */
    private final UserType expectedType;
}
