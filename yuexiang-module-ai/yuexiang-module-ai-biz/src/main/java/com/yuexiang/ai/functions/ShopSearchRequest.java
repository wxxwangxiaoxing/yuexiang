package com.yuexiang.ai.functions;

import com.yuexiang.ai.domain.vo.RecommendedShopVO;
import lombok.Data;

import java.util.List;
import java.util.function.Function;

@Data
public class ShopSearchRequest {

    private String cuisine;
    private Integer budgetMin;
    private Integer budgetMax;
    private String location;
    private String occasion;
    private Integer radius = 3000;
    private String sortBy;
    private List<String> features;
    private List<Long> excludeIds;
}
