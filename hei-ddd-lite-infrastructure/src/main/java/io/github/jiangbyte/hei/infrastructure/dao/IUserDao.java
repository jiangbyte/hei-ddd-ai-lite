package io.github.jiangbyte.hei.infrastructure.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.jiangbyte.hei.infrastructure.dao.po.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表 Mapper。
 */
@Mapper
public interface IUserDao extends BaseMapper<User> {
}
