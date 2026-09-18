package io.github.jiangbyte.hei.domain.model;

import io.github.jiangbyte.hei.domain.core.AggregateRoot;
import io.github.jiangbyte.hei.domain.core.DomainException;
import io.github.jiangbyte.hei.domain.port.PasswordHasher;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 用户聚合根：前台 / 后台账号，不含 RBAC。
 */
@Getter
public class User extends AggregateRoot<Long> {

    private static final long serialVersionUID = 1L;

    private final Long id;
    private final String username;
    private final String passwordHash;
    private final UserType userType;
    private final boolean enabled;
    private final LocalDateTime createTime;
    private final LocalDateTime updateTime;

    private User(Long id, String username, String passwordHash, UserType userType, boolean enabled,
                 LocalDateTime createTime, LocalDateTime updateTime) {
        this.id = id;
        this.username = Objects.requireNonNull(username, "username");
        this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash");
        this.userType = Objects.requireNonNull(userType, "userType");
        this.enabled = enabled;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    /**
     * 创建用户（id 由持久化分配）。
     */
    public static User create(String username, String passwordHash, UserType userType) {
        if (username == null || username.isBlank()) {
            throw new DomainException("用户名不能为空");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new DomainException("密码哈希不能为空");
        }
        if (userType == null) {
            throw new DomainException("用户类型不能为空");
        }
        LocalDateTime now = LocalDateTime.now();
        return new User(null, username.trim(), passwordHash, userType, true, now, now);
    }

    /**
     * 从持久化状态还原。
     */
    public static User restore(Long id, String username, String passwordHash, UserType userType,
                               boolean enabled, LocalDateTime createTime, LocalDateTime updateTime) {
        return new User(id, username, passwordHash, userType, enabled, createTime, updateTime);
    }

    /**
     * 变更启用状态。
     */
    public User changeEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return this;
        }
        return new User(id, username, passwordHash, userType, enabled, createTime, LocalDateTime.now());
    }

    /**
     * 校验明文密码；账号禁用或不匹配时返回 false。
     */
    public boolean authenticate(String rawPassword, PasswordHasher hasher) {
        if (!enabled) {
            return false;
        }
        if (rawPassword == null || hasher == null) {
            return false;
        }
        return hasher.matches(rawPassword, passwordHash);
    }

    public boolean isAdmin() {
        return userType == UserType.ADMIN;
    }

    public boolean isPortal() {
        return userType == UserType.PORTAL;
    }
}
