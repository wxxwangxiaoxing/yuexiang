package com.yuexiang.shop.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "ShopListQueryDTO", description = "商户列表查询参数")
public class ShopListQueryDTO {

    @Schema(description = "商户类型ID")
    private Long typeId;

    @Schema(description = "搜索关键词（匹配商户名称）")
    private String keyword;

    @Schema(description = "商圈区域")
    private String area;

    @Schema(description = "排序方式: distance/score/price_asc/price_desc/ai")
    private String sortBy = "score";

    @Schema(description = "用户经度（sortBy=distance时必传）")
    private Double longitude;

    @Schema(description = "用户纬度（sortBy=distance时必传）")
    private Double latitude;

    @Schema(description = "最低人均（元）")
    private Integer minPrice;

    @Schema(description = "最高人均（元）")
    private Integer maxPrice;

    @Schema(description = "页码", example = "1")
    private Integer pageNo = 1;

    @Schema(description = "每页条数（上限20）", example = "10")
    private Integer pageSize = 10;
}
