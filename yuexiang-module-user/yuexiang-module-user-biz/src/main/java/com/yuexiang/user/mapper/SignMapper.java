package com.yuexiang.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuexiang.user.domain.entity.Sign;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SignMapper extends BaseMapper<Sign> {

    @Select("SELECT * FROM tb_sign WHERE user_id = #{userId} AND year = #{year} AND month = #{month} AND deleted = 0")
    Sign selectByUserMonth(@Param("userId") Long userId, @Param("year") Integer year, @Param("month") Integer month);

    @Update("UPDATE tb_sign SET sign_bitmap = #{bitmap}, sign_days = #{signDays}, update_time = NOW() WHERE id = #{id}")
    int updateBitmap(@Param("id") Long id, @Param("bitmap") long bitmap, @Param("signDays") Integer signDays);

    @Update("UPDATE tb_sign SET sign_bitmap = #{newBitmap}, sign_days = #{signDays}, update_time = NOW() WHERE id = #{id} AND sign_bitmap = #{oldBitmap}")
    int updateBitmapWithCAS(@Param("id") Long id, @Param("newBitmap") Integer newBitmap, @Param("signDays") Integer signDays, @Param("oldBitmap") Integer oldBitmap);
}
