package io.github.jiangbyte.hei.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页响应。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private long total;
    private int pageNo;
    private int pageSize;
    private List<T> records;
}
