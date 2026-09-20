package io.github.jiangbyte.hei.trigger.web;

import io.github.jiangbyte.hei.api.IAuthService;
import io.github.jiangbyte.hei.api.dto.AuthRequest;
import io.github.jiangbyte.hei.api.response.AuthRegisterResponse;
import io.github.jiangbyte.hei.api.response.AuthTokenResponse;
import io.github.jiangbyte.hei.api.response.R;
import io.github.jiangbyte.hei.api.response.UserProfileResponse;
import io.github.jiangbyte.hei.application.AuthApplicationService;
import io.github.jiangbyte.hei.application.command.LoginCommand;
import io.github.jiangbyte.hei.application.command.RegisterUserCommand;
import io.github.jiangbyte.hei.application.dto.AuthResultView;
import io.github.jiangbyte.hei.application.query.GetMyProfileQuery;
import io.github.jiangbyte.hei.domain.user.model.valobj.UserType;
import io.github.jiangbyte.hei.trigger.assembler.UserAssembler;
import io.github.jiangbyte.hei.trigger.config.OpenApiConfiguration;
import io.github.jiangbyte.hei.trigger.security.AuthContext;
import io.github.jiangbyte.hei.trigger.security.JwtProperties;
import io.github.jiangbyte.hei.trigger.security.JwtTokenProvider;
import io.github.jiangbyte.hei.trigger.security.LoginUser;
import io.github.jiangbyte.hei.trigger.security.RequireLogin;
import io.github.jiangbyte.hei.trigger.security.UnauthorizedException;
import io.github.jiangbyte.hei.types.enums.ResponseCode;
import io.github.jiangbyte.hei.types.exception.BizException;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;

/**
 * 认证接口：注册（前台）、登录（按端类型）、登出、当前用户。
 */
@Tag(name = "认证")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController implements IAuthService {

    private final AuthApplicationService authApplicationService;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final UserAssembler userAssembler;

    @Override
    @PostMapping("/register")
    public R<AuthRegisterResponse> register(@RequestBody AuthRequest request) {
        // 1. 委托应用服务注册
        AuthResultView result = authApplicationService.register(
                new RegisterUserCommand(request.getUsername(), request.getPassword()));
        // 2. 组装契约响应（无 Token）
        return R.ok(AuthRegisterResponse.builder()
                .userId(result.getUserId())
                .username(result.getUsername())
                .userType(result.getUserType().name())
                .build());
    }

    @Override
    @PostMapping("/login")
    public R<AuthTokenResponse> login(@RequestBody AuthRequest request) {
        // 1. 解析端类型并登录
        UserType clientType = parseClientType(request.getClientType());
        AuthResultView result = authApplicationService.login(
                new LoginCommand(request.getUsername(), request.getPassword(), clientType));
        // 2. 签发 JWT 并组装契约响应
        String token = jwtTokenProvider.createToken(
                String.valueOf(result.getUserId()),
                result.getUsername(),
                result.getUserType().name());
        return R.ok(AuthTokenResponse.builder()
                .userId(result.getUserId())
                .username(result.getUsername())
                .userType(result.getUserType().name())
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getExpireSeconds())
                .build());
    }

    @Override
    @RequireLogin
    @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH)
    @PostMapping("/logout")
    public R<Void> logout() {
        // 1. 从登录上下文取 jti
        LoginUser loginUser = AuthContext.get();
        if (loginUser == null || loginUser.getJti() == null) {
            throw new UnauthorizedException("未登录或 Token 无效");
        }
        // 2. 从当前请求头解析 Token，计算剩余 TTL 后拉黑
        HttpServletRequest request = currentRequest();
        String token = request == null
                ? null
                : jwtTokenProvider.resolveToken(request.getHeader(jwtProperties.getHeader()));
        Duration ttl = token == null ? Duration.ZERO : jwtTokenProvider.remainingTtl(token);
        authApplicationService.logout(loginUser.getJti(), ttl);
        return R.ok();
    }

    @Override
    @RequireLogin
    @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH)
    @GetMapping("/me")
    public R<UserProfileResponse> me() {
        Long userId = parseUserId(AuthContext.getUserId());
        return R.ok(userAssembler.toProfileResponse(
                authApplicationService.getMyProfile(new GetMyProfileQuery(userId))));
    }

    private static HttpServletRequest currentRequest() {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes servletAttrs) {
            return servletAttrs.getRequest();
        }
        return null;
    }

    private static UserType parseClientType(String clientType) {
        if (clientType == null || clientType.isBlank()) {
            return UserType.PORTAL;
        }
        try {
            return UserType.from(clientType);
        } catch (IllegalArgumentException ex) {
            throw new BizException(ResponseCode.VALIDATION_ERROR, "clientType 仅支持 PORTAL 或 ADMIN");
        }
    }

    private static Long parseUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new UnauthorizedException("未登录");
        }
        try {
            return Long.valueOf(userId);
        } catch (NumberFormatException ex) {
            throw new BizException(ResponseCode.UNAUTHORIZED, "用户标识无效");
        }
    }
}
