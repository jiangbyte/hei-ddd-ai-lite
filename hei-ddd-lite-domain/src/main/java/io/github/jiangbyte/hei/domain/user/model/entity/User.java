package io.github.jiangbyte.hei.domain.user.model.entity;

import io.github.jiangbyte.hei.domain.core.AggregateRoot;
import io.github.jiangbyte.hei.domain.core.DomainException;
import io.github.jiangbyte.hei.domain.user.event.UserCreatedEvent;
import io.github.jiangbyte.hei.domain.user.event.UserEnabledChangedEvent;
import io.github.jiangbyte.hei.domain.user.adapter.port.PasswordHasher;
import io.github.jiangbyte.hei.domain.user.model.valobj.UserType;
import io.github.jiangbyte.hei.domain.user.model.valobj.Username;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 用户聚合根：前台 / 后台账号（不含 RBAC），作为脚手架 DDD 竖切示例。
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
     * 创建用户（id 由持久化分配；创建事件在 {@link #markCreated()} 登记）。
     */
    public static User create(Username username, String passwordHash, UserType userType) {
        if (username == null) {
            throw new DomainException("用户名不能为空");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new DomainException("密码哈希不能为空");
        }
        if (userType == null) {
            throw new DomainException("用户类型不能为空");
        }
        LocalDateTime now = LocalDateTime.now();
        return new User(null, username.getValue(), passwordHash, userType, true, now, now);
    }

    /**
     * 从持久化状态还原（不携带未发布事件）。
     */
    public static User restore(Long id, String username, String passwordHash, UserType userType,
                               boolean enabled, LocalDateTime createTime, LocalDateTime updateTime) {
        return new User(id, username, passwordHash, userType, enabled, createTime, updateTime);
    }

    /**
     * 持久化分配 ID 后登记创建事件。
     */
    public void markCreated() {
        // 1. 必须已有持久化标识
        if (id == null) {
            throw new DomainException("用户尚未持久化，无法登记创建事件");
        }
        // 2. 登记创建事实，供应用服务 pull 后发布
        registerEvent(new UserCreatedEvent(id, username, userType));
    }

    /**
     * 变更启用状态；有实际变更时登记领域事件。
     */
    public User changeEnabled(boolean enabled) {
        // 1. 状态未变则返回自身，避免无意义写库与事件
        if (this.enabled == enabled) {
            return this;
        }
        // 2. 构造新状态并登记变更事件
        User next = new User(id, username, passwordHash, userType, enabled, createTime, LocalDateTime.now());
        next.registerEvent(new UserEnabledChangedEvent(id, enabled));
        return next;
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
