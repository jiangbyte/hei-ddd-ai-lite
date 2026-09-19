package io.github.jiangbyte.hei.interfaces.web;

import io.github.jiangbyte.hei.api.IUserService;
import io.github.jiangbyte.hei.api.response.PublicUserResponse;
import io.github.jiangbyte.hei.api.response.R;
import io.github.jiangbyte.hei.application.AuthApplicationService;
import io.github.jiangbyte.hei.application.query.GetPublicUserQuery;
import io.github.jiangbyte.hei.interfaces.assembler.UserAssembler;
import io.github.jiangbyte.hei.types.enums.ResponseCode;
import io.github.jiangbyte.hei.types.exception.BizException;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户公开信息接口。
 */
@Tag(name = "用户公开")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController implements IUserService {

    private final AuthApplicationService authApplicationService;
    private final UserAssembler userAssembler;

    /**
     * 按 userId 获取用户公开信息（无需登录；query 传参）。
     */
    @Override
    @GetMapping("/public")
    public R<PublicUserResponse> getPublic(@RequestParam Long userId) {
        if (userId == null) {
            throw new BizException(ResponseCode.VALIDATION_ERROR, "userId 不能为空");
        }
        return R.ok(userAssembler.toPublicResponse(
                authApplicationService.getPublicProfile(new GetPublicUserQuery(userId))));
    }
}
