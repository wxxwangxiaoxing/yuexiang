package com.yuexiang.shop.domain.vo;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.Data;

import java.util.List;

@Data
public class ShopDetailVO {

    private Long    id;
    private String  name;
    private String  typeName;
    private List<String> images;
    private String  area;
    private String  address;
    private Double  longitude;
    private Double  latitude;
    private String  avgPrice;
    private Integer salesCount;
    private Integer commentCount;
    private Double  score;
    private Integer reviewCount;
    @JsonRawValue
    private String  openHours;
    private Boolean isFavorite;
    private Boolean isOpen;
    private String  phone;
}