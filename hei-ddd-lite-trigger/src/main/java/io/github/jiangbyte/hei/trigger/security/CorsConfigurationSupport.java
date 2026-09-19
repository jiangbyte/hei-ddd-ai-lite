package io.github.jiangbyte.hei.trigger.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * CORS 跨域自动装配。
 */
@Configuration
@EnableConfigurationProperties(CorsProperties.class)
@ConditionalOnProperty(prefix = "hei.ddd.cors", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CorsConfigurationSupport {

    /**
     * 注册全局 CORS 过滤器。
     */
    @Bean
    public CorsFilter corsFilter(CorsProperties properties) {
        CorsConfiguration config = new CorsConfiguration();
        // allowCredentials=true 时不能使用 allowedOrigins=*，需改用 pattern
        if (properties.isAllowCredentials()) {
            for (String origin : properties.getAllowedOrigins()) {
                config.addAllowedOriginPattern(origin);
            }
            config.setAllowCredentials(true);
        } else {
            for (String origin : properties.getAllowedOrigins()) {
                if ("*".equals(origin)) {
                    config.addAllowedOriginPattern("*");
                } else {
                    config.addAllowedOrigin(origin);
                }
            }
            config.setAllowCredentials(false);
        }
        properties.getAllowedHeaders().forEach(config::addAllowedHeader);
        properties.getAllowedMethods().forEach(config::addAllowedMethod);
        config.setMaxAge(properties.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
