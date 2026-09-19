package io.github.jiangbyte.hei.application.query;

import io.github.jiangbyte.hei.application.core.Query;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 查询用户公开信息。
 */
@Getter
@AllArgsConstructor
public class GetPublicUserQuery implements Query {

    private final Long userId;
}
