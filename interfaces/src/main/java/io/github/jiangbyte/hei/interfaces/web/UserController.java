package io.github.jiangbyte.hei.interfaces.web;

import io.github.jiangbyte.hei.application.AuthApplicationService;
import io.github.jiangbyte.hei.application.query.GetPublicUserQuery;
import io.github.jiangbyte.hei.interfaces.assembler.UserAssembler;
import io.github.jiangbyte.hei.interfaces.response.PublicUserResponse;
import io.github.jiangbyte.hei.interfaces.response.R;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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
     * 按 ID 获取用户公开信息（无需登录）。
     */
    @GetMapping("/{id}")
    public R<PublicUserResponse> getPublic(@PathVariable Long id) {
        return R.ok(userAssembler.toPublicResponse(
                authApplicationService.getPublicProfile(new GetPublicUserQuery(id))));
    }
}
