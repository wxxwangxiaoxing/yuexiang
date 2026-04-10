package com.yuexiang.voucher.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_refund_record")
public class RefundRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String refundNo;

    private String thirdRefundNo;

    private Long orderId;

    private Long userId;

    private Long refundAmount;

    private String reason;

    private Integer status;

    private Long auditBy;

    private LocalDateTime auditTime;

    private LocalDateTime refundTime;

    private String rejectReason;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
