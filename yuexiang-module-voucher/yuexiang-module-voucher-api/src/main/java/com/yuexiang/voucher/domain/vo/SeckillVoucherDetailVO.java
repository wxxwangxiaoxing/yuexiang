package com.yuexiang.voucher.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(name = "SeckillVoucherDetailVO", description = "秒杀券详情响应")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillVoucherDetailVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "优惠券ID")
    private Long voucherId;

    @Schema(description = "所属商户ID")
    private Long shopId;

    @Schema(description = "商户名称")
    private String shopName;

    @Schema(description = "商户首图")
    private String shopImage;

    @Schema(description = "商户地址")
    private String shopAddress;

    @Schema(description = "券标题")
    private String title;

    @Schema(description = "副标题")
    private String subTitle;

    @Schema(description = "使用规则")
    private String rules;

    @Schema(description = "秒杀支付金额（分）")
    private Integer payValue;

    @Schema(description = "抵扣金额（分）")
    private Integer actualValue;

    @Schema(description = "初始总库存")
    private Integer totalStock;

    @Schema(description = "剩余库存")
    private Integer remainStock;

    @Schema(description = "库存百分比 0~100")
    private Integer stockPercent;

    @Schema(description = "秒杀开始时间（毫秒时间戳）")
    private Long beginTime;

    @Schema(description = "秒杀结束时间（毫秒时间戳）")
    private Long endTime;

    @Schema(description = "用券开始时间（毫秒时间戳）")
    private Long validBeginTime;

    @Schema(description = "用券截止时间（毫秒时间戳）")
    private Long validEndTime;

    @Schema(description = "秒杀状态：0=未开始 1=进行中 2=已结束 3=已抢光")
    private Integer seckillStatus;

    @Schema(description = "状态文案")
    private String seckillStatusDesc;

    @Schema(description = "当前用户是否已购买")
    private Boolean hasBought;

    @Schema(description = "服务器时间（毫秒时间戳）")
    private Long serverTime;
}
