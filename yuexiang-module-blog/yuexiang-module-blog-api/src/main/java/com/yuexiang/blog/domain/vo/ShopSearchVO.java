package com.yuexiang.blog.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@Schema(name = "ShopSearchVO", description = "商户搜索响应")
public class ShopSearchVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "商户列表")
    private List<ShopVO> list;

    @Schema(description = "总数")
    private Long total;

    @Schema(description = "当前页码")
    private Integer page;

    @Schema(description = "每页条数")
    private Integer size;

    @Schema(description = "总页数")
    private Integer pages;

    @Data
    @Schema(name = "ShopVO", description = "商户信息")
    public static class ShopVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "商户ID")
        private Long id;

        @Schema(description = "商户名称")
        private String name;

        @Schema(description = "商户类型名称")
        private String typeName;

        @Schema(description = "地址")
        private String address;

        @Schema(description = "人均价格(分)")
        private Integer avgPrice;

        @Schema(description = "评分")
        private String score;
    }
}
