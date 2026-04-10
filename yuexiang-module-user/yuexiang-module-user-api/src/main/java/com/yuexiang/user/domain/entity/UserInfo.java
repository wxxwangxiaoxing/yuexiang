package com.yuexiang.user.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("tb_user_info")
public class UserInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String city;

    private String introduce;

    private Integer fansCount;

    private Integer followCount;

    private Integer likeCount;

    private Integer maxContinuousSignDays;

    private Integer gender;

    private LocalDate birthday;

    @TableField("level")
    private Integer level;

    private Integer points;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
