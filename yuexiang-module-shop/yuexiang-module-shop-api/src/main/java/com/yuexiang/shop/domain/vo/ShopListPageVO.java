package com.yuexiang.shop.domain.vo;

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
@Schema(name = "ShopListPageVO", description = "商户列表分页结果")
public class ShopListPageVO {

    @Schema(description = "总记录数")
    private Long total;

    @Schema(description = "总页数")
    private Integer pages;

    @Schema(description = "当前页码")
    private Integer current;

    @Schema(description = "商户列表")
    private List<ShopListVO> records;
}
