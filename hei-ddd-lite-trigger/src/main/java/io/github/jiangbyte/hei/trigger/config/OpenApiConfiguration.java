package io.github.jiangbyte.hei.trigger.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j / springdoc OpenAPI 基础信息与 Bearer JWT 鉴权方案。
 */
@Configuration
public class OpenApiConfiguration {

    public static final String BEARER_AUTH = "BearerAuth";

    @Bean
    public OpenAPI heiOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("hei-ddd-ai-lite API")
                        .description("个人 AI demo 脚手架接口文档（Knife4j + OpenAPI 3）")
                        .version("1.0.0")
                        .contact(new Contact().name("hei")))
                .components(new Components().addSecuritySchemes(
                        BEARER_AUTH,
                        new SecurityScheme()
                                .name(BEARER_AUTH)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("登录后获取的 Bearer Token，格式：Bearer {token}")));
    }
}
