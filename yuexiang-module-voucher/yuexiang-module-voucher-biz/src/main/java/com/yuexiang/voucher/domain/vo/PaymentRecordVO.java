package com.yuexiang.voucher.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaymentRecordVO {

    private Long id;

    private String paymentNo;

    private String thirdPaymentNo;

    private Long orderId;

    private Long amount;

    private Integer payType;

    private String payTypeDesc;

    private Integer status;

    private String statusDesc;

    private LocalDateTime createTime;

    private LocalDateTime finishTime;
}
