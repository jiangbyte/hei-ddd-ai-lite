package io.github.jiangbyte.hei.application;

import io.github.jiangbyte.hei.application.command.LoginCommand;
import io.github.jiangbyte.hei.application.command.RegisterUserCommand;
import io.github.jiangbyte.hei.application.core.ApplicationService;
import io.github.jiangbyte.hei.application.dto.AuthResultView;
import io.github.jiangbyte.hei.application.dto.PublicUserView;
import io.github.jiangbyte.hei.application.dto.UserProfileView;
import io.github.jiangbyte.hei.application.query.GetMyProfileQuery;
import io.github.jiangbyte.hei.application.query.GetPublicUserQuery;
import io.github.jiangbyte.hei.domain.core.BizException;
import io.github.jiangbyte.hei.domain.core.DomainEventPublisher;
import io.github.jiangbyte.hei.domain.core.DomainException;
import io.github.jiangbyte.hei.domain.factory.UserFactory;
import io.github.jiangbyte.hei.domain.model.User;
import io.github.jiangbyte.hei.domain.model.UserType;
import io.github.jiangbyte.hei.domain.port.PasswordHasher;
import io.github.jiangbyte.hei.domain.port.TokenDenylist;
import io.github.jiangbyte.hei.domain.repository.UserRepository;
import io.github.jiangbyte.hei.domain.service.UserClientAccessPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

/**
 * 认证与用户查询应用服务：注册 / 登录 / 资料 / 登出。
 */
@Service
@RequiredArgsConstructor
public class AuthApplicationService implements ApplicationService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenDenylist tokenDenylist;
    private final DomainEventPublisher domainEventPublisher;
    private final UserClientAccessPolicy userClientAccessPolicy;

    /**
     * 前台注册：固定创建 PORTAL 用户，保存后发布创建事件。
     */
    @Transactional
    public AuthResultView register(RegisterUserCommand command) {
        // 1. 工厂创建合法聚合（含用户名规约与密码哈希）
        User toSave;
        try {
            toSave = new UserFactory(passwordHasher).createPortal(command.getUsername(), command.getPassword());
        } catch (DomainException ex) {
            throw new BizException("VALIDATION_ERROR", ex.getMessage());
        }
        // 2. 用户名唯一性
        if (userRepository.existsByUsername(toSave.getUsername())) {
            throw new BizException("USERNAME_TAKEN", "用户名已存在");
        }
        // 3. 持久化 → 登记创建事件 → 发布
        User saved = userRepository.save(toSave);
        saved.markCreated();
        domainEventPublisher.publish(saved.pullDomainEvents());
        return toAuthResult(saved);
    }

    /**
     * 登录：校验密码，并由领域服务校验期望端类型（前台仅 PORTAL，后台仅 ADMIN）。
     */
    @Transactional(readOnly = true)
    public AuthResultView login(LoginCommand command) {
        // 1. 校验入参完整性（应用层入参，非领域不变式）
        if (command.getUsername() == null || command.getUsername().isBlank()) {
            throw new BizException("VALIDATION_ERROR", "用户名不能为空");
        }
        if (command.getPassword() == null || command.getPassword().isBlank()) {
            throw new BizException("VALIDATION_ERROR", "密码不能为空");
        }
        // 2. 查用户并校验口令；失败统一模糊提示，避免泄露账号是否存在
        UserType expected = command.getExpectedType() == null ? UserType.PORTAL : command.getExpectedType();
        String username = command.getUsername().trim();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BizException("INVALID_CREDENTIALS", "用户名或密码错误"));
        if (!user.authenticate(command.getPassword(), passwordHasher)) {
            throw new BizException("INVALID_CREDENTIALS", "用户名或密码错误");
        }
        // 3. 领域服务判定端类型访问权；映射为业务错误码供接口层返回
        try {
            userClientAccessPolicy.assertCanAccess(user, expected);
        } catch (DomainException ex) {
            throw new BizException("FORBIDDEN_CLIENT", ex.getMessage());
        }
        // 4. 组装登录结果读模型
        return toAuthResult(user);
    }

    @Transactional(readOnly = true)
    public UserProfileView getMyProfile(GetMyProfileQuery query) {
        User user = userRepository.findById(query.getUserId())
                .orElseThrow(() -> new BizException("USER_NOT_FOUND", "用户不存在"));
        return toProfileView(user);
    }

    /**
     * 公开资料仅暴露前台启用用户。
     */
    @Transactional(readOnly = true)
    public PublicUserView getPublicProfile(GetPublicUserQuery query) {
        if (query.getUserId() == null) {
            throw new BizException("VALIDATION_ERROR", "userId 不能为空");
        }
        User user = userRepository.findById(query.getUserId())
                .filter(User::isEnabled)
                .filter(User::isPortal)
                .orElseThrow(() -> new BizException("USER_NOT_FOUND", "用户不存在"));
        return new PublicUserView(user.getId(), user.getUsername(), user.getUserType());
    }

    public void logout(String jti, Duration remainingTtl) {
        if (jti == null || jti.isBlank()) {
            throw new BizException("UNAUTHORIZED", "Token 无效");
        }
        Duration ttl = remainingTtl == null || remainingTtl.isNegative() || remainingTtl.isZero()
                ? Duration.ofSeconds(1)
                : remainingTtl;
        tokenDenylist.deny(jti, ttl);
    }

    static AuthResultView toAuthResult(User user) {
        return new AuthResultView(user.getId(), user.getUsername(), user.getUserType());
    }

    static UserProfileView toProfileView(User user) {
        return new UserProfileView(
                user.getId(),
                user.getUsername(),
                user.getUserType(),
                user.isEnabled(),
                user.getCreateTime(),
                user.getUpdateTime());
    }
}
