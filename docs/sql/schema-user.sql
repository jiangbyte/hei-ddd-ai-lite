-- 用户表（前台 PORTAL / 后台 ADMIN，无 RBAC）
CREATE TABLE IF NOT EXISTS sys_user (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username      VARCHAR(64)  NOT NULL COMMENT '用户名',
    password_hash VARCHAR(100) NOT NULL COMMENT 'BCrypt 密码哈希',
    user_type     VARCHAR(16)  NOT NULL DEFAULT 'PORTAL' COMMENT 'PORTAL|ADMIN',
    enabled       TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否启用',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_username (username),
    KEY idx_sys_user_type (user_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户';

-- 已有库升级
-- ALTER TABLE sys_user ADD COLUMN user_type VARCHAR(16) NOT NULL DEFAULT 'PORTAL' COMMENT 'PORTAL|ADMIN' AFTER password_hash;
-- ALTER TABLE sys_user ADD KEY idx_sys_user_type (user_type);

-- 默认后台账号 admin / admin123（BCrypt）
INSERT INTO sys_user (username, password_hash, user_type, enabled)
VALUES ('admin', '$2b$10$rZWIIpx0AqpzCcbg/Pbtu.h41mYpoTNy2FNnjC19OVy7NPuUIvNzK', 'ADMIN', 1)
ON DUPLICATE KEY UPDATE user_type = 'ADMIN', enabled = 1;
