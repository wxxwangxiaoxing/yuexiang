package com.yuexiang.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuexiang.system.domain.entity.SmsCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SmsCodeMapper extends BaseMapper<SmsCode> {

    SmsCode selectLatest(@Param("phone") String phone, @Param("type") Integer type);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
