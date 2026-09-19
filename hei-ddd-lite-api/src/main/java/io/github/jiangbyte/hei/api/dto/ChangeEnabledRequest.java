package io.github.jiangbyte.hei.api.dto;

import lombok.Data;

/**
 * 变更用户启用状态请求（body：userId + enabled）。
 */
@Data
public class ChangeEnabledRequest {

    private Long userId;
    private Boolean enabled;
}
