package com.yuexiang.voucher.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "创建优惠券请求")
public class VoucherCreateDTO {

    @Schema(description = "优惠券标题", required = true)
    private String title;

    @Schema(description = "副标题")
    private String subTitle;

    @Schema(description = "使用规则")
    private String rules;

    @Schema(description = "支付金额（分）", required = true)
    private Integer payValue;

    @Schema(description = "抵扣金额（分）", required = true)
    private Integer actualValue;

    @Schema(description = "券类型：0普通券，1秒杀券", required = true)
    private Integer type;

    @Schema(description = "用券开始时间")
    private LocalDateTime validBeginTime;

    @Schema(description = "用券截止时间")
    private LocalDateTime validEndTime;

    @Schema(description = "秒杀库存（type=1时必填）")
    private Integer stock;

    @Schema(description = "秒杀开始时间（type=1时必填）")
    private LocalDateTime beginTime;

    @Schema(description = "秒杀结束时间（type=1时必填）")
    private LocalDateTime endTime;
}
