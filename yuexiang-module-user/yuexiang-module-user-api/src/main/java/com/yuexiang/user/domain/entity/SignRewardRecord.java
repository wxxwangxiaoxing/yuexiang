package com.yuexiang.user.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_sign_reward_record")
public class SignRewardRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long ruleId;

    private String cycleMonth;

    private Integer rewardType;

    private Integer rewardValue;

    private Integer status;

    private Long bizId;

    private LocalDateTime createTime;
}
