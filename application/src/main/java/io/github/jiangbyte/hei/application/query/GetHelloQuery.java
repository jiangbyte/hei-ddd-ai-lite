package io.github.jiangbyte.hei.application.query;

import io.github.jiangbyte.hei.application.core.Query;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 按 ID 查询 Hello 只读用例入参。
 */
@Getter
@AllArgsConstructor
public class GetHelloQuery implements Query {

    private final Long helloId;
}
