package com.yuexiang.shop.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ShopAreaVO", description = "商圈区域")
public class ShopAreaVO {

    @Schema(description = "商圈区域名称")
    private String area;

    @Schema(description = "该区域商户数量")
    private Integer shopCount;
}
