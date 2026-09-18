package io.github.jiangbyte.hei.domain.repository;

import io.github.jiangbyte.hei.domain.core.Repository;
import io.github.jiangbyte.hei.domain.model.User;
import io.github.jiangbyte.hei.domain.model.UserType;

import java.util.List;
import java.util.Optional;

/**
 * 用户仓储端口。
 */
public interface UserRepository extends Repository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    /**
     * 分页查询用户。
     *
     * @return 当前页数据；总数通过 {@link #count(String, UserType)} 获取
     */
    List<User> findPage(int pageNo, int pageSize, String usernameLike, UserType userType);

    long count(String usernameLike, UserType userType);
}
