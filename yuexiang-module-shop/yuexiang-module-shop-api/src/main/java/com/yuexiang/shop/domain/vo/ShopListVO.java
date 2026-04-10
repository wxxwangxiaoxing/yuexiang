package com.yuexiang.shop.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(name = "ShopListVO", description = "商户列表项")
public class ShopListVO {

    @Schema(description = "商户ID")
    private Long id;

    @Schema(description = "商户名称")
    private String name;

    @Schema(description = "商户图片列表（最多3张）")
    private List<String> images;

    @Schema(description = "类型名称")
    private String typeName;

    @Schema(description = "商圈区域")
    private String area;

    @Schema(description = "详细地址")
    private String address;

    @Schema(description = "经度")
    private Double longitude;

    @Schema(description = "纬度")
    private Double latitude;

    @Schema(description = "综合评分（0.0~5.0）")
    private Double score;

    @Schema(description = "人均价格（元）")
    private Integer avgPrice;

    @Schema(description = "销量")
    private Integer salesCount;

    @Schema(description = "评论数")
    private Integer commentCount;

    @Schema(description = "距离（米，仅传入经纬度时返回）")
    private Integer distance;

    @Schema(description = "标签列表（最多3个）")
    private List<String> tags;

    @Schema(description = "AI一句话点评")
    private String aiSummary;

    @Schema(description = "当前用户是否收藏")
    private Boolean isFavorite;
}
