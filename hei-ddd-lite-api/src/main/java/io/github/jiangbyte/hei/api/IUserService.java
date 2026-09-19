package io.github.jiangbyte.hei.api;

import io.github.jiangbyte.hei.api.response.PublicUserResponse;
import io.github.jiangbyte.hei.api.response.R;

/**
 * 用户公开信息对外契约。
 */
public interface IUserService {

    /**
     * 按 userId 获取公开资料。
     */
    R<PublicUserResponse> getPublic(Long userId);
}
