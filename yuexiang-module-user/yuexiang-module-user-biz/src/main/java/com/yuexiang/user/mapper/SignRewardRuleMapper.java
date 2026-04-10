package com.yuexiang.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuexiang.user.domain.entity.SignRewardRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SignRewardRuleMapper extends BaseMapper<SignRewardRule> {

    @Select("SELECT * FROM tb_sign_reward_rule WHERE status = 1 ORDER BY sort ASC")
    List<SignRewardRule> selectAllEnabled();

    @Select("SELECT * FROM tb_sign_reward_rule WHERE required_days = #{requiredDays} AND status = 1 LIMIT 1")
    SignRewardRule selectByRequiredDays(Integer requiredDays);
}
