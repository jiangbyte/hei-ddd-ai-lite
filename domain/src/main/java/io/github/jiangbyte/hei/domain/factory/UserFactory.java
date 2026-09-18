package io.github.jiangbyte.hei.domain.factory;

import io.github.jiangbyte.hei.domain.core.DomainException;
import io.github.jiangbyte.hei.domain.core.Factory;
import io.github.jiangbyte.hei.domain.model.User;
import io.github.jiangbyte.hei.domain.model.UserType;
import io.github.jiangbyte.hei.domain.port.PasswordHasher;
import lombok.RequiredArgsConstructor;

/**
 * 用户工厂：按端类型组装合法 User 聚合。
 */
@RequiredArgsConstructor
public class UserFactory implements Factory {

    private final PasswordHasher passwordHasher;

    /**
     * 创建前台用户。
     */
    public User createPortal(String username, String rawPassword) {
        return create(username, rawPassword, UserType.PORTAL);
    }

    /**
     * 创建指定类型用户。
     */
    public User create(String username, String rawPassword, UserType userType) {
        if (username == null || username.isBlank()) {
            throw new DomainException("用户名不能为空");
        }
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new DomainException("密码不能为空");
        }
        if (userType == null) {
            throw new DomainException("用户类型不能为空");
        }
        return User.create(username.trim(), passwordHasher.hash(rawPassword), userType);
    }
}
