package com.yuexiang.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "我的订单VO")
public class MyOrderVO {

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "商户ID")
    private Long shopId;

    @Schema(description = "商户名称")
    private String shopName;

    @Schema(description = "商户图片")
    private String shopImage;

    @Schema(description = "优惠券标题")
    private String voucherTitle;

    @Schema(description = "支付金额（分）")
    private Integer payValue;

    @Schema(description = "抵扣金额（分）")
    private Integer actualValue;

    @Schema(description = "支付方式：0未支付 1余额 2微信 3支付宝")
    private Integer payType;

    @Schema(description = "支付方式文案")
    private String payTypeDesc;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "状态文案")
    private String statusDesc;

    @Schema(description = "下单时间")
    private Long createTime;

    @Schema(description = "支付时间")
    private Long payTime;

    @Schema(description = "使用时间")
    private Long useTime;

    @Schema(description = "支付截止时间（待支付时）")
    private Long payDeadline;

    @Schema(description = "可操作按钮列表")
    private List<String> actions;
}
