package io.github.jiangbyte.hei.application.query;

import io.github.jiangbyte.hei.application.core.Query;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 查询当前登录用户账号信息。
 */
@Getter
@AllArgsConstructor
public class GetMyProfileQuery implements Query {

    private final Long userId;
}
