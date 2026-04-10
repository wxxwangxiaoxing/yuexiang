package com.yuexiang.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuexiang.user.domain.entity.SignRepairRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SignRepairRecordMapper extends BaseMapper<SignRepairRecord> {

    @Select("SELECT COUNT(*) FROM tb_sign_repair_record WHERE user_id = #{userId} AND YEAR(create_time) = #{year} AND MONTH(create_time) = #{month}")
    int countByUserMonth(@Param("userId") Long userId, @Param("year") int year, @Param("month") int month);
}
