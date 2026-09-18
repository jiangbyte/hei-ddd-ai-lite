package io.github.jiangbyte.hei.domain.port;

/**
 * 密码哈希端口：由基础设施层实现（如 BCrypt），领域层只依赖抽象。
 */
public interface PasswordHasher {

    /**
     * 对明文密码生成哈希。
     */
    String hash(String rawPassword);

    /**
     * 校验明文是否与哈希匹配。
     */
    boolean matches(String rawPassword, String passwordHash);
}
