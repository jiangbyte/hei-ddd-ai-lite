package io.github.jiangbyte.hei.domain.factory;

import io.github.jiangbyte.hei.domain.core.DomainException;
import io.github.jiangbyte.hei.domain.core.Factory;
import io.github.jiangbyte.hei.domain.model.Greeting;
import io.github.jiangbyte.hei.domain.model.Hello;
import io.github.jiangbyte.hei.domain.specification.GreetingNotBlankSpecification;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Hello 聚合工厂：创建合法聚合并登记创建事件。
 */
public class HelloFactory implements Factory {

    private static final AtomicLong ID_SEQ = new AtomicLong(1);
    private final GreetingNotBlankSpecification greetingSpec = new GreetingNotBlankSpecification();

    /**
     * 按问候文案创建 Hello 聚合（脚手架占位 ID 序列）。
     */
    public Hello create(String greetingText) {
        // 1. 构造值对象（内部已做空白校验）
        Greeting greeting = Greeting.of(greetingText);
        // 2. 再用规约二次确认，展示 Specification 用法
        if (!greetingSpec.isSatisfiedBy(greeting)) {
            throw new DomainException("问候语不满足规约");
        }
        // 3. 分配占位标识并创建聚合（含创建事件）
        Long id = ID_SEQ.getAndIncrement();
        return Hello.create(id, greeting);
    }
}
