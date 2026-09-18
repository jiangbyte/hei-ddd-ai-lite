package io.github.jiangbyte.hei.interfaces.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一 API 响应包装。
 *
 * @param <T> 业务数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 业务状态码，成功一般为 0 或 "OK" */
    private String code;

    /** 提示信息 */
    private String message;

    /** 业务数据 */
    private T data;

    /**
     * 成功响应（无数据）。
     */
    public static <T> R<T> ok() {
        return new R<>("0", "success", null);
    }

    /**
     * 成功响应（带数据）。
     */
    public static <T> R<T> ok(T data) {
        return new R<>("0", "success", data);
    }

    /**
     * 失败响应。
     */
    public static <T> R<T> fail(String code, String message) {
        return new R<>(code, message, null);
    }
}
