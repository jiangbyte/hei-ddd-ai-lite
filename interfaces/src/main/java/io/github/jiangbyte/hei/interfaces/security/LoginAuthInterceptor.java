package io.github.jiangbyte.hei.interfaces.security;

import io.github.jiangbyte.hei.types.enums.ResponseCode;
import io.github.jiangbyte.hei.types.exception.BizException;
import io.github.jiangbyte.hei.domain.port.TokenDenylist;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录 / 管理员校验拦截器。
 */
@RequiredArgsConstructor
public class LoginAuthInterceptor implements HandlerInterceptor {

    private final JwtProperties properties;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenDenylist tokenDenylist;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        RequireAdmin requireAdmin = handlerMethod.getMethodAnnotation(RequireAdmin.class);
        if (requireAdmin == null) {
            requireAdmin = handlerMethod.getBeanType().getAnnotation(RequireAdmin.class);
        }
        RequireLogin requireLogin = handlerMethod.getMethodAnnotation(RequireLogin.class);
        if (requireLogin == null) {
            requireLogin = handlerMethod.getBeanType().getAnnotation(RequireLogin.class);
        }
        boolean needLogin = requireLogin != null || requireAdmin != null;
        if (!needLogin) {
            return true;
        }

        String header = request.getHeader(properties.getHeader());
        String token = jwtTokenProvider.resolveToken(header);
        if (token == null) {
            throw new UnauthorizedException("未登录或缺少 Token");
        }

        LoginUser loginUser = jwtTokenProvider.parseToken(token);
        if (tokenDenylist.isDenied(loginUser.getJti())) {
            throw new UnauthorizedException("Token 已失效，请重新登录");
        }
        if (requireAdmin != null && !loginUser.isAdmin()) {
            throw new BizException(ResponseCode.FORBIDDEN, "需要后台管理员权限");
        }
        AuthContext.set(loginUser);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContext.clear();
    }
}
