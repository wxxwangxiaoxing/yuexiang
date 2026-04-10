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
public class SignCalendarVO {

    /** 查询年份 */
    private Integer year;
    /** 查询月份 */
    private Integer month;
    /** 今天几号（非当月返回 null） */
    private Integer today;
    /** 当月总天数 */
    private Integer totalDaysInMonth;
    /** 当月 1 号星期几（1=周一，7=周日） */
    private Integer firstDayOfWeek;

    /** 已签到的日期列表 */
    private List<Integer> signedDays;
    /** 补签日期列表 */
    private List<Integer> repairedDays;
    /** 当月签到总天数 */
    private Integer signedCount;

    /** 当前连续签到天数 */
    private Integer continuousDays;
    /** 历史最长连签 */
    private Integer maxContinuousDays;

    /** 今日是否已签 */
    private Boolean isSignedToday;
    /** 今日可获积分 */
    private Integer todayPoints;
    /** 当月累计积分 */
    private Integer monthTotalPoints;

    /** 剩余补签次数 */
    private Integer repairRemain;
    /** 补签消耗积分 */
    private Integer repairCost;

    /** 下一个里程碑 */
    private NextMilestoneVO nextMilestone;
    /** 里程碑列表 */
    private List<MilestoneStatusVO> milestones;

    /** 服务器时间戳（毫秒） */
    private Long serverTime;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NextMilestoneVO {
        private Integer targetDays;
        private Integer remainDays;
        private String rewardName;
        private String rewardIcon;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MilestoneStatusVO {
        private Long ruleId;
        private Integer requiredDays;
        private Integer rewardType;
        private String rewardName;
        private String rewardIcon;
        /** 0-未达成 1-可领取 2-已领取 */
        private Integer status;
        private String statusDesc;
    }
}