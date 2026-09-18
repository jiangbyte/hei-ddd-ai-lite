package io.github.jiangbyte.hei.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * 分页结果。
 */
@Getter
@AllArgsConstructor
public class PageResult<T> {

    private final long total;
    private final int pageNo;
    private final int pageSize;
    private final List<T> records;
}
