package com.yuexiang.voucher.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("tb_seckill_session")
@Schema(name = "SeckillSession", description = "秒杀场次实体")
public class SeckillSession implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "场次ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "场次名称")
    private String title;

    @Schema(description = "活动日期")
    private LocalDate date;

    @Schema(description = "开始时间")
    @TableField("begin_time")
    private LocalDateTime beginTime;

    @Schema(description = "结束时间")
    @TableField("end_time")
    private LocalDateTime endTime;

    @Schema(description = "状态(0禁用,1启用)")
    private Integer status;

    @Schema(description = "创建时间")
    @TableField("create_time")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
