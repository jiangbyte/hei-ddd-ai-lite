package io.github.jiangbyte.hei.infrastructure.config.datasource;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * Druid 数据源约定自动配置。
 * <p>
 * 当 classpath 存在 DruidDataSource（通常随 druid-spring-boot-3-starter 引入）
 * 且 hei.ddd.datasource.enabled=true（默认）时生效。
 * 连接池由 Druid 自动配置创建，本类作为启用标记与扩展点。
 */
@AutoConfiguration
@ConditionalOnClass(name = "com.alibaba.druid.pool.DruidDataSource")
@ConditionalOnProperty(prefix = "hei.ddd.datasource", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DatasourceAutoConfiguration {
}
