package com.yuexiang.ai.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "推荐商户VO")
public class RecommendedShopVO {

    @Schema(description = "商户ID")
    private Long shopId;

    @Schema(description = "商户名称")
    private String name;

    @Schema(description = "商户图片")
    private String image;

    @Schema(description = "商户类型")
    private String typeName;

    @Schema(description = "评分")
    private BigDecimal score;

    @Schema(description = "人均消费")
    private Integer avgPrice;

    @Schema(description = "距离（米）")
    private Double distance;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "推荐理由")
    private String reason;
}
