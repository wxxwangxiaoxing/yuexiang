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
public class VoucherDetailVO {

    private Long id;

    private Long shopId;

    private String shopName;

    private String title;

    private String subTitle;

    private String rules;

    private Double payValue;

    private Double actualValue;

    private Integer type;

    private Integer status;

    private LocalDateTime validBeginTime;

    private LocalDateTime validEndTime;
}
