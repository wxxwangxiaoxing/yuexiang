package com.yuexiang.shop.domain.vo;

import lombok.Data;
import java.io.Serializable;

@Data
public class ShopTypeVO implements Serializable {
    private Long id;
    private String name;
    private String icon;
    private Integer sort;
}