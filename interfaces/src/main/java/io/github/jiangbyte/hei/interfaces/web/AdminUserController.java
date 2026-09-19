package io.github.jiangbyte.hei.interfaces.web;

import io.github.jiangbyte.hei.application.AdminUserApplicationService;
import io.github.jiangbyte.hei.application.command.ChangeUserEnabledCommand;
import io.github.jiangbyte.hei.application.command.CreateUserCommand;
import io.github.jiangbyte.hei.application.query.ListUsersQuery;
import io.github.jiangbyte.hei.domain.core.BizException;
import io.github.jiangbyte.hei.domain.model.UserType;
import io.github.jiangbyte.hei.interfaces.assembler.UserAssembler;
import io.github.jiangbyte.hei.interfaces.config.OpenApiConfiguration;
import io.github.jiangbyte.hei.interfaces.response.PageResponse;
import io.github.jiangbyte.hei.interfaces.response.R;
import io.github.jiangbyte.hei.interfaces.response.UserProfileResponse;
import io.github.jiangbyte.hei.interfaces.security.RequireAdmin;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 后台用户管理接口（需 ADMIN）：分页列表、创建、启用/禁用。
 * <p>
 * 作为脚手架 DDD 竖切的 Web 示例：组装 Command/Query，委托应用服务，经 Assembler 输出。
 */
@Tag(name = "后台用户")
@SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH)
@RestController
@RequestMapping("/admin/users")
@RequireAdmin
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserApplicationService adminUserApplicationService;
    private final UserAssembler userAssembler;

    /**
     * 分页查询用户（query：pageNo / pageSize / username / userType）。
     */
    @GetMapping
    public R<PageResponse<UserProfileResponse>> list(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String userType) {
        UserType type = parseOptionalType(userType);
        return R.ok(userAssembler.toPageResponse(
                adminUserApplicationService.listUsers(new ListUsersQuery(pageNo, pageSize, username, type))));
    }

    /**
     * 创建用户（body：username / password / userType）。
     */
    @PostMapping
    public R<Map<String, Object>> create(@RequestBody CreateUserRequest request) {
        if (request == null) {
            throw new BizException("VALIDATION_ERROR", "请求体不能为空");
        }
        UserType type = parseOptionalType(request.getUserType());
        if (type == null) {
            type = UserType.PORTAL;
        }
        var result = adminUserApplicationService.createUser(
                new CreateUserCommand(request.getUsername(), request.getPassword(), type));
        Map<String, Object> data = new HashMap<>();
        data.put("userId", result.getUserId());
        data.put("username", result.getUsername());
        data.put("userType", result.getUserType().name());
        return R.ok(data);
    }

    /**
     * 变更启用状态（POST + body，避免 path 变量与 PUT）。
     */
    @PostMapping("/change-enabled")
    public R<UserProfileResponse> changeEnabled(@RequestBody ChangeEnabledRequest request) {
        if (request == null) {
            throw new BizException("VALIDATION_ERROR", "请求体不能为空");
        }
        if (request.getUserId() == null) {
            throw new BizException("VALIDATION_ERROR", "userId 不能为空");
        }
        if (request.getEnabled() == null) {
            throw new BizException("VALIDATION_ERROR", "enabled 不能为空");
        }
        return R.ok(userAssembler.toProfileResponse(
                adminUserApplicationService.changeEnabled(
                        new ChangeUserEnabledCommand(request.getUserId(), request.getEnabled()))));
    }

    private static UserType parseOptionalType(String userType) {
        if (userType == null || userType.isBlank()) {
            return null;
        }
        try {
            return UserType.valueOf(userType.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BizException("VALIDATION_ERROR", "userType 仅支持 PORTAL 或 ADMIN");
        }
    }
}
