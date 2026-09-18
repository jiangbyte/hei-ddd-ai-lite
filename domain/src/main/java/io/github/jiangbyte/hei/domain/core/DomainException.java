package io.github.jiangbyte.hei.domain.core;

/**
 * 领域异常：表示领域规则被违反或领域操作失败。
 */
public class DomainException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
