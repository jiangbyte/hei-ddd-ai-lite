package io.github.jiangbyte.hei.domain.user.service;

import io.github.jiangbyte.hei.domain.core.DomainException;
import io.github.jiangbyte.hei.domain.core.DomainService;
import io.github.jiangbyte.hei.domain.user.model.entity.User;
import io.github.jiangbyte.hei.domain.user.model.valobj.UserType;

/**
 * 用户端访问策略：判定已认证用户能否登录指定端（PORTAL / ADMIN）。
 * <p>
 * 规则不属于单一聚合行为，故以无状态领域服务表达。
 */
public final class UserClientAccessPolicy implements DomainService {

    /**
     * 断言用户类型与期望登录端一致；不一致则抛领域异常。
     */
    public void assertCanAccess(User user, UserType expectedClient) {
        // 1. 入参完整性：缺用户或期望端时无法判定
        if (user == null) {
            throw new DomainException("用户不能为空");
        }
        if (expectedClient == null) {
            throw new DomainException("期望登录端不能为空");
        }
        // 2. 端类型匹配：PORTAL 仅进前台，ADMIN 仅进后台
        if (user.getUserType() == expectedClient) {
            return;
        }
        // 3. 按期望端给出明确拒绝原因，供应用层映射为业务错误码
        if (expectedClient == UserType.ADMIN) {
            throw new DomainException("非后台账号，无法登录管理端");
        }
        throw new DomainException("非前台账号，无法登录用户端");
    }
}
