package io.github.jiangbyte.hei.interfaces.assembler;

import io.github.jiangbyte.hei.application.dto.HelloView;
import io.github.jiangbyte.hei.interfaces.response.HelloResponse;
import org.springframework.stereotype.Component;

/**
 * Hello 组装器：应用读模型 → API 响应。
 */
@Component
public class HelloAssembler implements Assembler {

    /**
     * 将应用层视图转为接口响应。
     */
    public HelloResponse toResponse(HelloView view) {
        if (view == null) {
            return null;
        }
        return new HelloResponse(view.getId(), view.getGreetingText());
    }
}
