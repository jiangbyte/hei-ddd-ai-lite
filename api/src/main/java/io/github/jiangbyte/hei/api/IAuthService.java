package io.github.jiangbyte.hei.api;

import io.github.jiangbyte.hei.api.dto.AuthRequest;
import io.github.jiangbyte.hei.api.response.AuthRegisterResponse;
import io.github.jiangbyte.hei.api.response.AuthTokenResponse;
import io.github.jiangbyte.hei.api.response.R;
import io.github.jiangbyte.hei.api.response.UserProfileResponse;

/**
 * 认证对外契约。
 */
public interface IAuthService {

    /**
     * 前台注册。
     */
    R<AuthRegisterResponse> register(AuthRequest request);

    /**
     * 按端类型登录。
     */
    R<AuthTokenResponse> login(AuthRequest request);

    /**
     * 登出（需登录）。
     */
    R<Void> logout();

    /**
     * 当前登录用户资料（需登录）。
     */
    R<UserProfileResponse> me();
}
