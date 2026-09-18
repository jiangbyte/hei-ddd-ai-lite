package io.github.jiangbyte.hei.domain.port;

import java.time.Duration;

/**
 * Token 黑名单端口：登出后将 jti 失效，由基础设施（如 Redis）实现。
 */
public interface TokenDenylist {

    /**
     * 将 Token 标识加入黑名单，TTL 建议为 Token 剩余有效期。
     */
    void deny(String jti, Duration ttl);

    /**
     * 是否已在黑名单中。
     */
    boolean isDenied(String jti);
}
