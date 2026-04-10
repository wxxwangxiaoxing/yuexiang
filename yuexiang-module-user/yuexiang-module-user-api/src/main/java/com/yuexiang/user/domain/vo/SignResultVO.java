package com.yuexiang.user.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignResultVO {

    private String signDate;
    private Integer day;
    private Integer basePoints;
    private Integer bonusPoints;
    private Integer totalRewardPoints;
    private Integer userTotalPoints;
    private Integer continuousDays;
    private Boolean isBonus;
    private String bonusDesc;
    private MilestoneVO unlockedMilestone;
    private String animation;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MilestoneVO {
        private Long ruleId;
        private Integer requiredDays;
        private String rewardName;
        private String rewardIcon;
        private String message;
    }
}
