package com.yuexiang.shop.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopRecommendVO {

    private Long shopId;
    private String name;
    private String image;
    private String typeName;
    private BigDecimal score;
    private Integer avgPrice;
    private Double distance;
    private String address;
}
