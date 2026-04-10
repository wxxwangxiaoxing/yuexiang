package com.yuexiang.voucher.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RefundRecordVO {

    private Long id;

    private String refundNo;

    private String orderNo;

    private Long orderId;

    private Long refundAmount;

    private String reason;

    private Integer status;

    private String statusDesc;

    private String rejectReason;

    private LocalDateTime createTime;

    private LocalDateTime refundTime;
}
