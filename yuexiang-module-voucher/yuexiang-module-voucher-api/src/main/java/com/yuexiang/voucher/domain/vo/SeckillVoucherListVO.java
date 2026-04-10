package com.yuexiang.voucher.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@Schema(name = "SeckillVoucherListVO", description = "秒杀券列表响应")
public class SeckillVoucherListVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "总数")
    private Long total;

    @Schema(description = "当前页码")
    private Integer page;

    @Schema(description = "每页条数")
    private Integer pageSize;

    @Schema(description = "是否有更多")
    private Boolean hasMore;

    @Schema(description = "券列表")
    private List<VoucherVO> list;

    @Data
    @Schema(name = "VoucherVO", description = "秒杀券信息")
    public static class VoucherVO implements Serializable {

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

        @Schema(description = "券标题")
        private String title;

        @Schema(description = "副标题")
        private String subTitle;

        @Schema(description = "秒杀支付金额（分）")
        private Integer payValue;

        @Schema(description = "抵扣金额（分）")
        private Integer actualValue;

        @Schema(description = "原价（分）")
        private Integer originalPrice;

        @Schema(description = "秒杀价（分）")
        private Integer seckillPrice;

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

        @Schema(description = "秒杀状态：0=未开始 1=进行中 2=已结束 3=已抢光")
        private Integer seckillStatus;

        @Schema(description = "状态文案")
        private String seckillStatusDesc;
    }
}
