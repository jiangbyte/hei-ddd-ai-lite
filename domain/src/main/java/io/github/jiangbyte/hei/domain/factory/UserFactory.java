package io.github.jiangbyte.hei.domain.factory;

import io.github.jiangbyte.hei.domain.core.DomainException;
import io.github.jiangbyte.hei.domain.core.Factory;
import io.github.jiangbyte.hei.domain.model.User;
import io.github.jiangbyte.hei.domain.model.UserType;
import io.github.jiangbyte.hei.domain.model.Username;
import io.github.jiangbyte.hei.domain.port.PasswordHasher;
import io.github.jiangbyte.hei.domain.specification.UsernameFormatSpecification;
import lombok.RequiredArgsConstructor;

/**
 * 用户工厂：校验用户名规约、哈希密码，组装合法 User 聚合。
 */
@RequiredArgsConstructor
public class UserFactory implements Factory {

    private static final int MIN_PASSWORD_LENGTH = 6;

    private final PasswordHasher passwordHasher;
    private final UsernameFormatSpecification usernameSpec = new UsernameFormatSpecification();

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
        // 1. 构造用户名值对象（空白 / 长度 / 字符集）
        Username name = Username.of(username);
        // 2. 规约二次确认，展示 Specification 用法
        if (!usernameSpec.isSatisfiedBy(name)) {
            throw new DomainException("用户名不满足格式规约");
        }
        // 3. 校验明文密码
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new DomainException("密码不能为空");
        }
        if (rawPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new DomainException("密码长度至少 " + MIN_PASSWORD_LENGTH + " 位");
        }
        if (userType == null) {
            throw new DomainException("用户类型不能为空");
        }
        // 4. 哈希后创建聚合（创建事件在持久化后 markCreated）
        return User.create(name, passwordHasher.hash(rawPassword), userType);
    }
}
