package io.github.jiangbyte.hei.interfaces.web;

import lombok.Data;

/**
 * 变更启用状态请求。
 */
@Data
public class ChangeEnabledRequest {

    private Boolean enabled;
}
