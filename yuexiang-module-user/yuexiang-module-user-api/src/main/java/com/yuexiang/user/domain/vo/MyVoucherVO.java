package com.yuexiang.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "我的优惠券VO")
public class MyVoucherVO {

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "优惠券ID")
    private Long voucherId;

    @Schema(description = "商户ID")
    private Long shopId;

    @Schema(description = "商户名称")
    private String shopName;

    @Schema(description = "商户图片")
    private String shopImage;

    @Schema(description = "券标题")
    private String title;

    @Schema(description = "副标题")
    private String subTitle;

    @Schema(description = "支付金额（分）")
    private Integer payValue;

    @Schema(description = "抵扣金额（分）")
    private Integer actualValue;

    @Schema(description = "状态：0待支付 1已支付(未使用) 2已使用 3已退款 4已取消")
    private Integer status;

    @Schema(description = "状态文案")
    private String statusDesc;

    @Schema(description = "用券开始时间")
    private Long validBeginTime;

    @Schema(description = "用券截止时间")
    private Long validEndTime;

    @Schema(description = "剩余有效天数（-1表示已过期）")
    private Integer remainDays;

    @Schema(description = "下单时间")
    private Long createTime;
}
