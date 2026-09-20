package io.github.jiangbyte.hei.domain.user.model.valobj;

import io.github.jiangbyte.hei.domain.core.DomainException;
import io.github.jiangbyte.hei.domain.core.ValueObject;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * 用户名值对象：不可变，按文本相等；约束长度与字符集。
 */
@Getter
@EqualsAndHashCode
@ToString(of = "value", includeFieldNames = false)
public final class Username implements ValueObject {

    public static final int MIN_LENGTH = 3;
    public static final int MAX_LENGTH = 64;

    private final String value;

    private Username(String value) {
        this.value = value;
    }

    /**
     * 创建用户名；空白、长度或字符不合法时抛领域异常。
     */
    public static Username of(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new DomainException("用户名不能为空");
        }
        String trimmed = raw.trim();
        if (trimmed.length() < MIN_LENGTH || trimmed.length() > MAX_LENGTH) {
            throw new DomainException("用户名长度须在 " + MIN_LENGTH + "~" + MAX_LENGTH + " 之间");
        }
        if (!trimmed.matches("^[a-zA-Z0-9_]+$")) {
            throw new DomainException("用户名仅允许字母、数字与下划线");
        }
        return new Username(trimmed);
    }
}
