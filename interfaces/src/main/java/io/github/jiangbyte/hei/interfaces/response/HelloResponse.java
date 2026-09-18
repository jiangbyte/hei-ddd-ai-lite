package io.github.jiangbyte.hei.interfaces.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Hello API 响应体。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HelloResponse {

    private Long id;
    private String greetingText;
}
