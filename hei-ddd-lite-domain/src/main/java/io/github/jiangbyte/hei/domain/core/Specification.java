package io.github.jiangbyte.hei.domain.core;

/**
 * 规约：封装可复用的业务判定条件，保持聚合与领域服务可读。
 *
 * @param <T> 被判定对象类型
 */
public interface Specification<T> {

    /**
     * 判断候选对象是否满足本规约。
     */
    boolean isSatisfiedBy(T candidate);
}
