package io.github.jiangbyte.hei.trigger.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 配置属性。
 */
@Data
@ConfigurationProperties(prefix = "hei.ddd.jwt")
public class JwtProperties {

    /** 是否启用 JWT 登录校验拦截器 */
    private boolean enabled = true;

    /** HMAC 签名密钥，生产环境务必更换为足够长的随机串 */
    private String secret = "hei-ddd-ai-lite-jwt-secret-change-me-32bytes!!";

    /** Token 过期时间（秒），默认 2 小时 */
    private long expireSeconds = 7200;

    /** 请求头名称 */
    private String header = "Authorization";

    /** Token 前缀，例如 Bearer  */
    private String tokenPrefix = "Bearer ";

    /** 签发者 */
    private String issuer = "hei-ddd-ai-lite";
}
