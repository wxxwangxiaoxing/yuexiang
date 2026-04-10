package com.yuexiang.user.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignRewardsVO {

    private List<DailyPointsRuleVO> dailyPointsRules;
    private List<MilestoneRuleVO> milestoneRules;
    private RepairRulesVO repairRules;
    private String cycleDesc;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyPointsRuleVO {
        private Integer minDays;
        private Integer maxDays;
        private Integer points;
        private String desc;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MilestoneRuleVO {
        private Long ruleId;
        private Integer requiredDays;
        private Integer rewardType;
        private String rewardTypeDesc;
        private String rewardName;
        private String rewardIcon;
        private Integer rewardValue;
        private Integer bonusPoints;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RepairRulesVO {
        private Integer costPoints;
        private Integer maxPerMonth;
        private Integer repairWindowDays;
        private String description;
    }
}
