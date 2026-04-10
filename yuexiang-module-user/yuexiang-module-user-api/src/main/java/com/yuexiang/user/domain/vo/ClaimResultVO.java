package com.yuexiang.user.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimResultVO {

    private Long ruleId;
    private Integer rewardType;
    private String rewardName;
    private Object rewardDetail;
    private String message;
}
