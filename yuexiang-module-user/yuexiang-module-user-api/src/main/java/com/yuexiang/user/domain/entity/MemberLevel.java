package com.yuexiang.user.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_member_level")
public class MemberLevel {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer level;

    private String name;

    private String icon;

    private Integer minPoints;

    private BigDecimal discount;

    private String privileges;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
