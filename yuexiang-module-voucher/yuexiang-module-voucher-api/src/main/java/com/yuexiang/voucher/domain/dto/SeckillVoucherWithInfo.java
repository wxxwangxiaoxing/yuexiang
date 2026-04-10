package com.yuexiang.voucher.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SeckillVoucherWithInfo {

    private Long voucherId;

    private Long sessionId;

    private Integer stock;

    private Integer totalStock;

    private LocalDateTime beginTime;

    private LocalDateTime endTime;

    private Long shopId;

    private String title;

    private String subTitle;

    private Integer payValue;

    private Integer actualValue;
}
