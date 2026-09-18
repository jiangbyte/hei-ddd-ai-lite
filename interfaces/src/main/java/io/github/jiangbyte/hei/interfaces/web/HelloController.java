package io.github.jiangbyte.hei.interfaces.web;

import io.github.jiangbyte.hei.application.HelloApplicationService;
import io.github.jiangbyte.hei.application.command.CreateHelloCommand;
import io.github.jiangbyte.hei.application.query.GetHelloQuery;
import io.github.jiangbyte.hei.interfaces.assembler.HelloAssembler;
import io.github.jiangbyte.hei.interfaces.response.HelloResponse;
import io.github.jiangbyte.hei.interfaces.response.R;
import io.github.jiangbyte.hei.interfaces.security.AuthContext;
import io.github.jiangbyte.hei.interfaces.security.RequireLogin;
import io.github.jiangbyte.hei.interfaces.config.OpenApiConfiguration;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Hello Web 接口：接收 HTTP，调用应用服务，返回统一响应；不写业务规则。
 */
@Tag(name = "Hello")
@RestController
@RequiredArgsConstructor
public class HelloController {

    private final HelloApplicationService helloApplicationService;
    private final HelloAssembler helloAssembler;

    /**
     * 问候接口（公开）：演示接口层 → 应用层调用链。
     */
    @GetMapping("/hello")
    public R<String> hello() {
        return R.ok(helloApplicationService.greet());
    }

    /**
     * 创建 Hello：走工厂 → 仓储 → 领域事件完整链路。
     */
    @PostMapping("/hello")
    public R<HelloResponse> create(@RequestParam(defaultValue = "hello world") String greeting) {
        return R.ok(helloAssembler.toResponse(
                helloApplicationService.create(new CreateHelloCommand(greeting))));
    }

    /**
     * 按 ID 查询 Hello。
     */
    @GetMapping("/hello/{id}")
    public R<HelloResponse> get(@PathVariable Long id) {
        return R.ok(helloAssembler.toResponse(
                helloApplicationService.get(new GetHelloQuery(id))));
    }

    /**
     * 需登录的问候接口：演示 {@link RequireLogin} + JWT 校验。
     */
    @RequireLogin
    @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH)
    @GetMapping("/hello/secure")
    public R<String> secureHello() {
        String username = AuthContext.get() == null ? "unknown" : AuthContext.get().getUsername();
        return R.ok(helloApplicationService.greet() + ", " + username);
    }
}
