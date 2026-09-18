package io.github.jiangbyte.hei.domain.model;

import io.github.jiangbyte.hei.domain.core.DomainException;
import io.github.jiangbyte.hei.domain.core.ValueObject;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * 问候语文案值对象：不可变，按文本内容相等。
 */
@Getter
@EqualsAndHashCode
@ToString(of = "text", includeFieldNames = false)
public final class Greeting implements ValueObject {

    private final String text;

    private Greeting(String text) {
        this.text = text;
    }

    /**
     * 创建问候语；空白文案视为非法。
     */
    public static Greeting of(String text) {
        if (text == null || text.isBlank()) {
            throw new DomainException("问候语不能为空");
        }
        return new Greeting(text.trim());
    }
}
