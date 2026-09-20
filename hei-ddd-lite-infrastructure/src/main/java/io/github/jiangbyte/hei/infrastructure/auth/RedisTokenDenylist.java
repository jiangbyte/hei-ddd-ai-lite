package io.github.jiangbyte.hei.infrastructure.auth;

import io.github.jiangbyte.hei.domain.user.adapter.port.TokenDenylist;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 基于 Redis 的 Token 黑名单。
 */
@Component
@RequiredArgsConstructor
public class RedisTokenDenylist implements TokenDenylist {

    private static final String KEY_PREFIX = "auth:token:deny:";

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void deny(String jti, Duration ttl) {
        if (jti == null || jti.isBlank()) {
            return;
        }
        Duration effective = ttl == null || ttl.isNegative() || ttl.isZero() ? Duration.ofSeconds(1) : ttl;
        stringRedisTemplate.opsForValue().set(KEY_PREFIX + jti, "1", effective);
    }

    @Override
    public boolean isDenied(String jti) {
        if (jti == null || jti.isBlank()) {
            return false;
        }
        Boolean exists = stringRedisTemplate.hasKey(KEY_PREFIX + jti);
        return Boolean.TRUE.equals(exists);
    }
}
