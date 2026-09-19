package io.github.jiangbyte.hei.domain.specification;

import io.github.jiangbyte.hei.domain.core.Specification;
import io.github.jiangbyte.hei.domain.model.Username;

/**
 * 用户名格式规约：与 {@link Username#of(String)} 约束一致，供工厂二次确认。
 */
public final class UsernameFormatSpecification implements Specification<Username> {

    @Override
    public boolean isSatisfiedBy(Username candidate) {
        if (candidate == null || candidate.getValue() == null) {
            return false;
        }
        String value = candidate.getValue();
        int len = value.length();
        return len >= Username.MIN_LENGTH
                && len <= Username.MAX_LENGTH
                && value.matches("^[a-zA-Z0-9_]+$");
    }
}
