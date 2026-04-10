package com.yuexiang.voucher.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_seckill_voucher")
public class SeckillVoucher {

    @TableId
    private Long voucherId;

    @TableField("session_id")
    private Long sessionId;

    private Integer totalStock;

    private Integer stock;

    private LocalDateTime beginTime;

    private LocalDateTime endTime;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
