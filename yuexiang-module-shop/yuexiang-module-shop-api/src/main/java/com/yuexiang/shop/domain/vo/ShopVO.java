package com.yuexiang.shop.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class ShopVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String images;

    private String area;

    private String address;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private Integer avgPrice;

    private Integer salesCount;

    private Integer commentCount;

    private BigDecimal score;

    private Integer reviewCount;

    private String openHours;

    private String typeName;

    private BigDecimal distance;
}
