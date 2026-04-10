package com.yuexiang.user.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_sign_reward_rule")
public class SignRewardRule {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer requiredDays;

    private Integer rewardType;

    private String rewardName;

    private String rewardIcon;

    private Integer rewardValue;

    private Long voucherId;

    private Integer bonusPoints;

    private String description;

    private Integer sort;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
