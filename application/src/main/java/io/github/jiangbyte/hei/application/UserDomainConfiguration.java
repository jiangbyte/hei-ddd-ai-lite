package io.github.jiangbyte.hei.application;

import io.github.jiangbyte.hei.domain.service.UserClientAccessPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 领域服务装配：domain 模块无 Spring，在此以 {@code @Bean} 注册无状态实现。
 */
@Configuration
public class UserDomainConfiguration {

    @Bean
    public UserClientAccessPolicy userClientAccessPolicy() {
        return new UserClientAccessPolicy();
    }
}
