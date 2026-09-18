package io.github.jiangbyte.hei.application;

import io.github.jiangbyte.hei.application.command.ChangeUserEnabledCommand;
import io.github.jiangbyte.hei.application.command.CreateUserCommand;
import io.github.jiangbyte.hei.application.core.ApplicationService;
import io.github.jiangbyte.hei.application.dto.AuthResultView;
import io.github.jiangbyte.hei.application.dto.PageResult;
import io.github.jiangbyte.hei.application.dto.UserProfileView;
import io.github.jiangbyte.hei.application.query.ListUsersQuery;
import io.github.jiangbyte.hei.domain.core.BizException;
import io.github.jiangbyte.hei.domain.factory.UserFactory;
import io.github.jiangbyte.hei.domain.model.User;
import io.github.jiangbyte.hei.domain.model.UserType;
import io.github.jiangbyte.hei.domain.port.PasswordHasher;
import io.github.jiangbyte.hei.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 后台用户管理应用服务。
 */
@Service
@RequiredArgsConstructor
public class AdminUserApplicationService implements ApplicationService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    @Transactional(readOnly = true)
    public PageResult<UserProfileView> listUsers(ListUsersQuery query) {
        int pageNo = Math.max(query.getPageNo(), 1);
        int pageSize = Math.min(Math.max(query.getPageSize(), 1), 100);
        String username = blankToNull(query.getUsername());
        UserType userType = query.getUserType();
        long total = userRepository.count(username, userType);
        List<UserProfileView> records = userRepository.findPage(pageNo, pageSize, username, userType).stream()
                .map(AuthApplicationService::toProfileView)
                .toList();
        return new PageResult<>(total, pageNo, pageSize, records);
    }

    @Transactional
    public AuthResultView createUser(CreateUserCommand command) {
        validateCredentials(command.getUsername(), command.getPassword());
        UserType userType = command.getUserType() == null ? UserType.PORTAL : command.getUserType();
        String username = command.getUsername().trim();
        if (userRepository.existsByUsername(username)) {
            throw new BizException("USERNAME_TAKEN", "用户名已存在");
        }
        User saved = userRepository.save(
                new UserFactory(passwordHasher).create(username, command.getPassword(), userType));
        return AuthApplicationService.toAuthResult(saved);
    }

    @Transactional
    public UserProfileView changeEnabled(ChangeUserEnabledCommand command) {
        User user = userRepository.findById(command.getUserId())
                .orElseThrow(() -> new BizException("USER_NOT_FOUND", "用户不存在"));
        User updated = userRepository.save(user.changeEnabled(command.isEnabled()));
        return AuthApplicationService.toProfileView(updated);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
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
}
