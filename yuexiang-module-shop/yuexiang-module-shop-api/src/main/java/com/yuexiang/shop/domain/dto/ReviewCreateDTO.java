package com.yuexiang.shop.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ReviewCreateDTO {

    @NotNull(message = "商户ID不能为空")
    private Long shopId;

    private Long orderId;

    @NotNull(message = "综合评分不能为空")
    @Min(value = 1, message = "最低评分为1")
    @Max(value = 5, message = "最高评分为5")
    private Integer score;

    @Min(value = 1, message = "最低评分为1")
    @Max(value = 5, message = "最高评分为5")
    private Integer scoreTaste;

    @Min(value = 1, message = "最低评分为1")
    @Max(value = 5, message = "最高评分为5")
    private Integer scoreEnv;

    @Min(value = 1, message = "最低评分为1")
    @Max(value = 5, message = "最高评分为5")
    private Integer scoreService;

    private String content;

    private List<String> images;
}
