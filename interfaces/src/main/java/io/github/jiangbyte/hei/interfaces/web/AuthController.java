package io.github.jiangbyte.hei.interfaces.web;

import io.github.jiangbyte.hei.application.AuthApplicationService;
import io.github.jiangbyte.hei.application.command.LoginCommand;
import io.github.jiangbyte.hei.application.command.RegisterUserCommand;
import io.github.jiangbyte.hei.application.dto.AuthResultView;
import io.github.jiangbyte.hei.application.query.GetMyProfileQuery;
import io.github.jiangbyte.hei.domain.core.BizException;
import io.github.jiangbyte.hei.domain.model.UserType;
import io.github.jiangbyte.hei.interfaces.assembler.UserAssembler;
import io.github.jiangbyte.hei.interfaces.config.OpenApiConfiguration;
import io.github.jiangbyte.hei.interfaces.response.R;
import io.github.jiangbyte.hei.interfaces.response.UserProfileResponse;
import io.github.jiangbyte.hei.interfaces.security.AuthContext;
import io.github.jiangbyte.hei.interfaces.security.JwtProperties;
import io.github.jiangbyte.hei.interfaces.security.JwtTokenProvider;
import io.github.jiangbyte.hei.interfaces.security.LoginUser;
import io.github.jiangbyte.hei.interfaces.security.RequireLogin;
import io.github.jiangbyte.hei.interfaces.security.UnauthorizedException;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证接口：注册（前台）、登录（按端类型）、登出、当前用户。
 */
@Tag(name = "认证")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthApplicationService authApplicationService;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final UserAssembler userAssembler;

    @PostMapping("/register")
    public R<Map<String, Object>> register(@RequestBody AuthRequest request) {
        AuthResultView result = authApplicationService.register(
                new RegisterUserCommand(request.getUsername(), request.getPassword()));
        return R.ok(authPayload(result, null));
    }

    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody AuthRequest request) {
        UserType clientType = parseClientType(request.getClientType());
        AuthResultView result = authApplicationService.login(
                new LoginCommand(request.getUsername(), request.getPassword(), clientType));
        String token = jwtTokenProvider.createToken(
                String.valueOf(result.getUserId()),
                result.getUsername(),
                result.getUserType().name());
        Map<String, Object> data = authPayload(result, token);
        data.put("tokenType", "Bearer");
        data.put("expiresIn", jwtProperties.getExpireSeconds());
        return R.ok(data);
    }

    @RequireLogin
    @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH)
    @PostMapping("/logout")
    public R<Void> logout(HttpServletRequest request) {
        LoginUser loginUser = AuthContext.get();
        if (loginUser == null || loginUser.getJti() == null) {
            throw new UnauthorizedException("未登录或 Token 无效");
        }
        String token = jwtTokenProvider.resolveToken(request.getHeader(jwtProperties.getHeader()));
        Duration ttl = token == null ? Duration.ZERO : jwtTokenProvider.remainingTtl(token);
        authApplicationService.logout(loginUser.getJti(), ttl);
        return R.ok();
    }

    @RequireLogin
    @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH)
    @GetMapping("/me")
    public R<UserProfileResponse> me() {
        Long userId = parseUserId(AuthContext.getUserId());
        return R.ok(userAssembler.toProfileResponse(
                authApplicationService.getMyProfile(new GetMyProfileQuery(userId))));
    }

    private Map<String, Object> authPayload(AuthResultView result, String token) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", result.getUserId());
        data.put("username", result.getUsername());
        data.put("userType", result.getUserType().name());
        if (token != null) {
            data.put("token", token);
        }
        return data;
    }

    private static UserType parseClientType(String clientType) {
        if (clientType == null || clientType.isBlank()) {
            return UserType.PORTAL;
        }
        try {
            return UserType.from(clientType);
        } catch (IllegalArgumentException ex) {
            throw new BizException("VALIDATION_ERROR", "clientType 仅支持 PORTAL 或 ADMIN");
        }
    }

    private static Long parseUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new UnauthorizedException("未登录");
        }
        try {
            return Long.valueOf(userId);
        } catch (NumberFormatException ex) {
            throw new BizException("UNAUTHORIZED", "用户标识无效");
        }
    }
}
