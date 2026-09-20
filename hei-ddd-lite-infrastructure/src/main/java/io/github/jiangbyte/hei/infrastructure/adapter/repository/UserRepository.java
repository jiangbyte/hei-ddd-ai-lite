package io.github.jiangbyte.hei.infrastructure.adapter.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.jiangbyte.hei.domain.user.adapter.repository.IUserRepository;
import io.github.jiangbyte.hei.domain.user.model.entity.User;
import io.github.jiangbyte.hei.domain.user.model.valobj.UserType;
import io.github.jiangbyte.hei.infrastructure.dao.IUserDao;
import io.github.jiangbyte.hei.infrastructure.dao.po.UserPo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户仓储 MyBatis-Plus 实现。
 */
@Repository
@RequiredArgsConstructor
public class UserRepository implements IUserRepository {

    private final IUserDao userDao;

    @Override
    public User save(User aggregate) {
        UserPo po = toPo(aggregate);
        if (po.getId() == null) {
            userDao.insert(po);
        } else {
            userDao.updateById(po);
        }
        return toDomain(po);
    }

    @Override
    public Optional<User> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(userDao.selectById(id)).map(this::toDomain);
    }

    @Override
    public void remove(Long id) {
        if (id != null) {
            userDao.deleteById(id);
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        UserPo po = userDao.selectOne(new LambdaQueryWrapper<UserPo>()
                .eq(UserPo::getUsername, username.trim()));
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        if (username == null || username.isBlank()) {
            return false;
        }
        Long count = userDao.selectCount(new LambdaQueryWrapper<UserPo>()
                .eq(UserPo::getUsername, username.trim()));
        return count != null && count > 0;
    }

    @Override
    public List<User> findPage(int pageNo, int pageSize, String usernameLike, UserType userType) {
        Page<UserPo> page = userDao.selectPage(new Page<>(pageNo, pageSize), buildWrapper(usernameLike, userType));
        return page.getRecords().stream().map(this::toDomain).toList();
    }

    @Override
    public long count(String usernameLike, UserType userType) {
        Long count = userDao.selectCount(buildWrapper(usernameLike, userType));
        return count == null ? 0L : count;
    }

    private static LambdaQueryWrapper<UserPo> buildWrapper(String usernameLike, UserType userType) {
        LambdaQueryWrapper<UserPo> wrapper = new LambdaQueryWrapper<>();
        if (usernameLike != null && !usernameLike.isBlank()) {
            wrapper.like(UserPo::getUsername, usernameLike.trim());
        }
        if (userType != null) {
            wrapper.eq(UserPo::getUserType, userType.name());
        }
        wrapper.orderByDesc(UserPo::getId);
        return wrapper;
    }

    private UserPo toPo(User user) {
        UserPo po = new UserPo();
        po.setId(user.getId());
        po.setUsername(user.getUsername());
        po.setPasswordHash(user.getPasswordHash());
        po.setUserType(user.getUserType().name());
        po.setEnabled(user.isEnabled());
        po.setCreateTime(user.getCreateTime());
        po.setUpdateTime(user.getUpdateTime());
        return po;
    }

    private User toDomain(UserPo po) {
        return User.restore(
                po.getId(),
                po.getUsername(),
                po.getPasswordHash(),
                UserType.from(po.getUserType()),
                Boolean.TRUE.equals(po.getEnabled()),
                po.getCreateTime(),
                po.getUpdateTime());
    }
}
