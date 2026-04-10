package com.yuexiang.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuexiang.user.domain.entity.SignRewardRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Set;

@Mapper
public interface SignRewardRecordMapper extends BaseMapper<SignRewardRecord> {

    @Select("SELECT COUNT(*) > 0 FROM tb_sign_reward_record WHERE user_id = #{userId} AND rule_id = #{ruleId} AND cycle_month = #{cycleMonth} AND status = 1")
    boolean existsByUserRuleCycle(@Param("userId") Long userId, @Param("ruleId") Long ruleId, @Param("cycleMonth") String cycleMonth);

    @Select("SELECT rule_id FROM tb_sign_reward_record WHERE user_id = #{userId} AND cycle_month = #{cycleMonth} AND status = 1")
    Set<Long> selectClaimedRuleIds(@Param("userId") Long userId, @Param("cycleMonth") String cycleMonth);
}
