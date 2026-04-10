package com.yuexiang.voucher.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(name = "SeckillOrderResultVO", description = "秒杀订单结果响应")
public class SeckillOrderResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "优惠券ID")
    private Long voucherId;

    @Schema(description = "商户名称")
    private String shopName;

    @Schema(description = "券标题")
    private String title;

    @Schema(description = "支付金额（分）")
    private Integer payValue;

    @Schema(description = "订单状态：-1=处理中 0=待支付 1=已支付 2=已使用 3=已退款 4=已取消")
    private Integer status;

    @Schema(description = "状态文案")
    private String statusDesc;

    @Schema(description = "创建时间（毫秒时间戳）")
    private Long createTime;

    @Schema(description = "支付截止时间（毫秒时间戳）")
    private Long payDeadline;
}
