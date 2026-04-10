package com.yuexiang.user.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_points_record")
public class PointsRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Integer type;

    private Integer points;

    private Integer balance;

    private String bizType;

    private Long bizId;

    private String description;

    private LocalDateTime createTime;
}
