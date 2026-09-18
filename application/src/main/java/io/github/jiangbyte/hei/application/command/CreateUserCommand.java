package io.github.jiangbyte.hei.application.command;

import io.github.jiangbyte.hei.application.core.Command;
import io.github.jiangbyte.hei.domain.model.UserType;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 后台创建用户。
 */
@Getter
@AllArgsConstructor
public class CreateUserCommand implements Command {

    private final String username;
    private final String password;
    private final UserType userType;
}
