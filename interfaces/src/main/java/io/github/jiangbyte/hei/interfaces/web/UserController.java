package io.github.jiangbyte.hei.interfaces.web;

import io.github.jiangbyte.hei.application.AuthApplicationService;
import io.github.jiangbyte.hei.application.query.GetPublicUserQuery;
import io.github.jiangbyte.hei.domain.core.BizException;
import io.github.jiangbyte.hei.interfaces.assembler.UserAssembler;
import io.github.jiangbyte.hei.interfaces.response.PublicUserResponse;
import io.github.jiangbyte.hei.interfaces.response.R;
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
public class UserController {

    private final AuthApplicationService authApplicationService;
    private final UserAssembler userAssembler;

    /**
     * 按 userId 获取用户公开信息（无需登录；query 传参）。
     */
    @GetMapping("/public")
    public R<PublicUserResponse> getPublic(@RequestParam Long userId) {
        if (userId == null) {
            throw new BizException("VALIDATION_ERROR", "userId 不能为空");
        }
        return R.ok(userAssembler.toPublicResponse(
                authApplicationService.getPublicProfile(new GetPublicUserQuery(userId))));
    }
}
