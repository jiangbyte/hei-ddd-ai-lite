package io.github.jiangbyte.hei.application.query;

import io.github.jiangbyte.hei.application.core.Query;
import io.github.jiangbyte.hei.domain.model.UserType;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 后台分页查询用户。
 */
@Getter
@AllArgsConstructor
public class ListUsersQuery implements Query {

    private final int pageNo;
    private final int pageSize;
    private final String username;
    private final UserType userType;
}
