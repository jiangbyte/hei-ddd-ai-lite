package io.github.jiangbyte.hei.trigger.assembler;

import io.github.jiangbyte.hei.api.response.PageResponse;
import io.github.jiangbyte.hei.api.response.PublicUserResponse;
import io.github.jiangbyte.hei.api.response.UserProfileResponse;
import io.github.jiangbyte.hei.application.dto.PageResult;
import io.github.jiangbyte.hei.application.dto.PublicUserView;
import io.github.jiangbyte.hei.application.dto.UserProfileView;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用户组装器：应用读模型 → API 响应。
 */
@Component
public class UserAssembler implements Assembler {

    public UserProfileResponse toProfileResponse(UserProfileView view) {
        if (view == null) {
            return null;
        }
        return new UserProfileResponse(
                view.getUserId(),
                view.getUsername(),
                view.getUserType().name(),
                view.isEnabled(),
                view.getCreateTime(),
                view.getUpdateTime());
    }

    public PublicUserResponse toPublicResponse(PublicUserView view) {
        if (view == null) {
            return null;
        }
        return new PublicUserResponse(view.getUserId(), view.getUsername(), view.getUserType().name());
    }

    public PageResponse<UserProfileResponse> toPageResponse(PageResult<UserProfileView> page) {
        List<UserProfileResponse> records = page.getRecords().stream().map(this::toProfileResponse).toList();
        return new PageResponse<>(page.getTotal(), page.getPageNo(), page.getPageSize(), records);
    }
}
