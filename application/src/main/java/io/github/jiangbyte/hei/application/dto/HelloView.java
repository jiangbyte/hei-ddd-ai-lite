package io.github.jiangbyte.hei.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Hello 用例读模型（应用层视图，供接口层组装响应）。
 */
@Getter
@AllArgsConstructor
public class HelloView {

    private final Long id;
    private final String greetingText;
}
