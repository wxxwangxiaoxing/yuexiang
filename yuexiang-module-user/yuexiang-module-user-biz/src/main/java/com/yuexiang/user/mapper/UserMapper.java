package com.yuexiang.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuexiang.user.domain.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    User selectByPhone(@Param("phone") String phone);
}
