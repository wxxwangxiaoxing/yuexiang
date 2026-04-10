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
public class SeckillVoucherVO {

    private Long voucherId;

    private Long shopId;

    private String shopName;

    private String title;

    private Double payValue;

    private Double actualValue;

    private Integer stock;

    private Integer totalStock;

    private LocalDateTime beginTime;

    private LocalDateTime endTime;

    private Long remainSeconds;

    private Integer status;

    private String image;
}
