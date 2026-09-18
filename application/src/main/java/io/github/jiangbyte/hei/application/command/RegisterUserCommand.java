package io.github.jiangbyte.hei.application.command;

import io.github.jiangbyte.hei.application.core.Command;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 注册用户写用例入参。
 */
@Getter
@AllArgsConstructor
public class RegisterUserCommand implements Command {

    private final String username;
    private final String password;
}
