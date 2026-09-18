package io.github.jiangbyte.hei.application.command;

import io.github.jiangbyte.hei.application.core.Command;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 创建 Hello 写用例入参。
 */
@Getter
@AllArgsConstructor
public class CreateHelloCommand implements Command {

    private final String greetingText;
}
