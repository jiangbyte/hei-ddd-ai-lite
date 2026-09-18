package io.github.jiangbyte.hei.domain.specification;

import io.github.jiangbyte.hei.domain.core.Specification;
import io.github.jiangbyte.hei.domain.model.Greeting;

/**
 * 问候语非空规约：占位示例，展示 Specification 落点。
 */
public final class GreetingNotBlankSpecification implements Specification<Greeting> {

    @Override
    public boolean isSatisfiedBy(Greeting candidate) {
        return candidate != null && candidate.getText() != null && !candidate.getText().isBlank();
    }
}
