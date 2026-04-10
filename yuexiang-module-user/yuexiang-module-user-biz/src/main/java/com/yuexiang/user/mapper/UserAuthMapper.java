package com.yuexiang.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuexiang.user.domain.entity.UserAuth;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserAuthMapper extends BaseMapper<UserAuth> {

    UserAuth selectByUserId(@Param("userId") Long userId);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status, 
                     @Param("rejectReason") String rejectReason);

    boolean existsByIdCard(@Param("idCard") String idCard, @Param("excludeUserId") Long excludeUserId);
}
