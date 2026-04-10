package com.yuexiang.voucher.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherOrderVO {

    private Long id;

    private String orderNo;

    private Long voucherId;

    private String voucherTitle;

    private Double payValue;

    private Double actualValue;

    private Integer status;

    private Integer payType;

    private LocalDateTime createTime;

    private LocalDateTime payTime;

    private LocalDateTime useTime;
}
