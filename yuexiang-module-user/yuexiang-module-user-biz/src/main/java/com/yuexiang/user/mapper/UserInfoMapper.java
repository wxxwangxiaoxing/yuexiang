package com.yuexiang.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuexiang.user.domain.entity.UserInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface UserInfoMapper extends BaseMapper<UserInfo> {

    int addPoints(@Param("userId") Long userId, @Param("points") Integer points);

    int deductPoints(@Param("userId") Long userId, @Param("points") Integer points);

    int updateLevel(@Param("userId") Long userId, @Param("level") Integer level);

    boolean insertOrUpdate(UserInfo userInfo);

    int updateLikeCount(@Param("userId") Long userId, @Param("delta") Integer delta);

//    int batchUpdateLikeCount(@Param("entries") List<Map<String, Object>> entries);

    Integer selectLikeCount(@Param("userId") Long userId);

    int updateLikeCountDirectly(@Param("userId") Long userId, @Param("likeCount") Long likeCount);

    List<Long> selectUserIdsByPage(@Param("offset") int offset, @Param("limit") int limit);

    @Select("SELECT points FROM tb_user_info WHERE user_id = #{userId} AND deleted = 0")
    Integer getPoints(@Param("userId") Long userId);

    @Update("UPDATE tb_user_info SET points = points + #{points}, update_time = NOW() WHERE user_id = #{userId} AND deleted = 0 AND points + #{points} >= 0")
    int addPointsSafe(@Param("userId") Long userId, @Param("points") Integer points);

    @Select("SELECT max_continuous_sign_days FROM tb_user_info WHERE user_id = #{userId} AND deleted = 0")
    Integer getMaxContinuousSignDays(@Param("userId") Long userId);

    @Update("UPDATE tb_user_info SET max_continuous_sign_days = #{days}, update_time = NOW() WHERE user_id = #{userId} AND (max_continuous_sign_days IS NULL OR max_continuous_sign_days < #{days}) AND deleted = 0")
    int updateMaxContinuousIfGreater(@Param("userId") Long userId, @Param("days") Integer days);
}
