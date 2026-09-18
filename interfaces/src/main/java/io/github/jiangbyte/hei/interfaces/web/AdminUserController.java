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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 后台用户管理接口（需 ADMIN）。
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

    @PostMapping
    public R<Map<String, Object>> create(@RequestBody CreateUserRequest request) {
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

    @PutMapping("/{id}/enabled")
    public R<UserProfileResponse> changeEnabled(@PathVariable Long id, @RequestBody ChangeEnabledRequest request) {
        if (request.getEnabled() == null) {
            throw new BizException("VALIDATION_ERROR", "enabled 不能为空");
        }
        return R.ok(userAssembler.toProfileResponse(
                adminUserApplicationService.changeEnabled(new ChangeUserEnabledCommand(id, request.getEnabled()))));
    }

    private static UserType parseOptionalType(String userType) {
        if (userType == null || userType.isBlank()) {
            return null;
        }
        try {
            return UserType.from(userType);
        } catch (IllegalArgumentException ex) {
            throw new BizException("VALIDATION_ERROR", "userType 仅支持 PORTAL 或 ADMIN");
        }
    }
}
