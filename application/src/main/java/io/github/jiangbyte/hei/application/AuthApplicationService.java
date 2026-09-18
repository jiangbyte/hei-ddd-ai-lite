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
import io.github.jiangbyte.hei.domain.factory.UserFactory;
import io.github.jiangbyte.hei.domain.model.User;
import io.github.jiangbyte.hei.domain.model.UserType;
import io.github.jiangbyte.hei.domain.port.PasswordHasher;
import io.github.jiangbyte.hei.domain.port.TokenDenylist;
import io.github.jiangbyte.hei.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

/**
 * 认证与用户查询应用服务。
 */
@Service
@RequiredArgsConstructor
public class AuthApplicationService implements ApplicationService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenDenylist tokenDenylist;

    /**
     * 前台注册：固定创建 PORTAL 用户。
     */
    @Transactional
    public AuthResultView register(RegisterUserCommand command) {
        validateCredentials(command.getUsername(), command.getPassword());
        String username = command.getUsername().trim();
        if (userRepository.existsByUsername(username)) {
            throw new BizException("USERNAME_TAKEN", "用户名已存在");
        }
        User saved = userRepository.save(
                new UserFactory(passwordHasher).createPortal(username, command.getPassword()));
        return toAuthResult(saved);
    }

    /**
     * 登录：校验密码，并校验期望端类型（前台仅 PORTAL，后台仅 ADMIN）。
     */
    @Transactional(readOnly = true)
    public AuthResultView login(LoginCommand command) {
        validateCredentials(command.getUsername(), command.getPassword());
        UserType expected = command.getExpectedType() == null ? UserType.PORTAL : command.getExpectedType();
        String username = command.getUsername().trim();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BizException("INVALID_CREDENTIALS", "用户名或密码错误"));
        if (!user.authenticate(command.getPassword(), passwordHasher)) {
            throw new BizException("INVALID_CREDENTIALS", "用户名或密码错误");
        }
        if (user.getUserType() != expected) {
            if (expected == UserType.ADMIN) {
                throw new BizException("FORBIDDEN_CLIENT", "非后台账号，无法登录管理端");
            }
            throw new BizException("FORBIDDEN_CLIENT", "非前台账号，无法登录用户端");
        }
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

    private static void validateCredentials(String username, String password) {
        if (username == null || username.isBlank()) {
            throw new BizException("VALIDATION_ERROR", "用户名不能为空");
        }
        if (password == null || password.isBlank()) {
            throw new BizException("VALIDATION_ERROR", "密码不能为空");
        }
        if (password.length() < 6) {
            throw new BizException("VALIDATION_ERROR", "密码长度至少 6 位");
        }
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
