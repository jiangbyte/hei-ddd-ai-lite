package io.github.jiangbyte.hei.trigger.security;

import io.github.jiangbyte.hei.domain.port.TokenDenylist;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * JWT 基础装配：始终注册 Token 工具（登录签发依赖）。
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtAuthConfiguration {

    /**
     * JWT 工具 Bean。
     */
    @Bean
    public JwtTokenProvider jwtTokenProvider(JwtProperties jwtProperties) {
        return new JwtTokenProvider(jwtProperties);
    }

    /**
     * 登录拦截器相关装配：仅在 hei.ddd.jwt.enabled=true（默认）时生效。
     */
    @Configuration
    @ConditionalOnProperty(prefix = "hei.ddd.jwt", name = "enabled", havingValue = "true", matchIfMissing = true)
    static class JwtInterceptorConfiguration {

        @Bean
        public LoginAuthInterceptor loginAuthInterceptor(JwtProperties jwtProperties,
                                                         JwtTokenProvider jwtTokenProvider,
                                                         TokenDenylist tokenDenylist) {
            return new LoginAuthInterceptor(jwtProperties, jwtTokenProvider, tokenDenylist);
        }

        @Bean
        public WebMvcConfigurer jwtAuthWebMvcConfigurer(LoginAuthInterceptor loginAuthInterceptor) {
            return new WebMvcConfigurer() {
                @Override
                public void addInterceptors(InterceptorRegistry registry) {
                    registry.addInterceptor(loginAuthInterceptor).addPathPatterns("/**");
                }
            };
        }
    }
}
