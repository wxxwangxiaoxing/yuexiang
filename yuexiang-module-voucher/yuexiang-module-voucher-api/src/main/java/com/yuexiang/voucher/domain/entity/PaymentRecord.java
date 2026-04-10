package com.yuexiang.voucher.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_payment_record")
public class PaymentRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String paymentNo;

    private String thirdPaymentNo;

    private Long userId;

    private Long orderId;

    private Long amount;

    private Integer payType;

    private Integer direction;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime finishTime;
}
