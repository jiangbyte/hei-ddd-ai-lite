package io.github.jiangbyte.hei.application;

import io.github.jiangbyte.hei.application.command.ChangeUserEnabledCommand;
import io.github.jiangbyte.hei.application.command.CreateUserCommand;
import io.github.jiangbyte.hei.application.core.ApplicationService;
import io.github.jiangbyte.hei.application.dto.AuthResultView;
import io.github.jiangbyte.hei.application.dto.PageResult;
import io.github.jiangbyte.hei.application.dto.UserProfileView;
import io.github.jiangbyte.hei.application.query.ListUsersQuery;
import io.github.jiangbyte.hei.types.exception.BizException;
import io.github.jiangbyte.hei.domain.core.DomainEventPublisher;
import io.github.jiangbyte.hei.domain.core.DomainException;
import io.github.jiangbyte.hei.domain.user.factory.UserFactory;
import io.github.jiangbyte.hei.domain.user.model.entity.User;
import io.github.jiangbyte.hei.domain.user.model.valobj.UserType;
import io.github.jiangbyte.hei.domain.user.adapter.port.PasswordHasher;
import io.github.jiangbyte.hei.domain.user.adapter.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 后台用户管理应用服务：分页查询、创建、启用/禁用。
 */
@Service
@RequiredArgsConstructor
public class AdminUserApplicationService implements ApplicationService {

    private final IUserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final DomainEventPublisher domainEventPublisher;

    @Transactional(readOnly = true)
    public PageResult<UserProfileView> listUsers(ListUsersQuery query) {
        // 1. 规范化分页与过滤条件
        int pageNo = Math.max(query.getPageNo(), 1);
        int pageSize = Math.min(Math.max(query.getPageSize(), 1), 100);
        String username = blankToNull(query.getUsername());
        UserType userType = query.getUserType();
        // 2. 查总数与当前页
        long total = userRepository.count(username, userType);
        List<UserProfileView> records = userRepository.findPage(pageNo, pageSize, username, userType).stream()
                .map(AuthApplicationService::toProfileView)
                .toList();
        return new PageResult<>(total, pageNo, pageSize, records);
    }

    @Transactional
    public AuthResultView createUser(CreateUserCommand command) {
        // 1. 工厂创建聚合
        User toSave;
        try {
            UserType userType = command.getUserType() == null ? UserType.PORTAL : command.getUserType();
            toSave = new UserFactory(passwordHasher)
                    .create(command.getUsername(), command.getPassword(), userType);
        } catch (DomainException ex) {
            throw new BizException("VALIDATION_ERROR", ex.getMessage());
        }
        // 2. 用户名唯一性
        if (userRepository.existsByUsername(toSave.getUsername())) {
            throw new BizException("USERNAME_TAKEN", "用户名已存在");
        }
        // 3. 持久化并发布创建事件
        User saved = userRepository.save(toSave);
        saved.markCreated();
        domainEventPublisher.publish(saved.pullDomainEvents());
        return AuthApplicationService.toAuthResult(saved);
    }

    @Transactional
    public UserProfileView changeEnabled(ChangeUserEnabledCommand command) {
        if (command.getUserId() == null) {
            throw new BizException("VALIDATION_ERROR", "userId 不能为空");
        }
        // 1. 加载聚合
        User user = userRepository.findById(command.getUserId())
                .orElseThrow(() -> new BizException("USER_NOT_FOUND", "用户不存在"));
        // 2. 变更状态（可能登记事件）
        User changed = user.changeEnabled(command.isEnabled());
        if (changed == user) {
            return AuthApplicationService.toProfileView(user);
        }
        // 3. 保存并发布事件
        User updated = userRepository.save(changed);
        domainEventPublisher.publish(changed.pullDomainEvents());
        return AuthApplicationService.toProfileView(updated);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
