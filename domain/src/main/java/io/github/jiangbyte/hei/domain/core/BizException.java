package io.github.jiangbyte.hei.domain.core;

import lombok.Getter;

/**
 * 业务可预期异常：携带业务错误码，供接口层映射为统一响应。
 */
@Getter
public class BizException extends DomainException {

    private static final long serialVersionUID = 1L;

    /** 业务错误码 */
    private final String code;

    public BizException(String message) {
        this("BIZ_ERROR", message);
    }

    public BizException(String code, String message) {
        super(message);
        this.code = code;
    }
}
