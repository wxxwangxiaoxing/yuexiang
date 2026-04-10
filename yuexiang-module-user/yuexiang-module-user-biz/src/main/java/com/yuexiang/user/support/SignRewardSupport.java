package com.yuexiang.user.support;

import com.yuexiang.user.domain.entity.SignRewardRule;
import com.yuexiang.user.domain.vo.ClaimResultVO;
import com.yuexiang.user.domain.vo.SignCalendarVO;
import com.yuexiang.user.domain.vo.SignResultVO;
import com.yuexiang.user.mapper.SignRewardRecordMapper;
import com.yuexiang.user.mapper.UserInfoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class SignRewardSupport {

    private final SignRewardRecordMapper rewardRecordMapper;
    private final UserInfoMapper userInfoMapper;
    private final SignPointsSupport signPointsSupport;

    public int determineMilestoneStatus(int requiredDays, Long ruleId, int continuousDays, Set<Long> claimedIds) {
        if (claimedIds.contains(ruleId)) {
            return 2;
        }
        if (continuousDays >= requiredDays) {
            return 1;
        }
        return 0;
    }

    public String getStatusDesc(int status) {
        return switch (status) {
            case 2 -> "已领取";
            case 1 -> "可领取";
            default -> "未达成";
        };
    }

    public SignCalendarVO.NextMilestoneVO findNextMilestone(
            List<SignRewardRule> rules,
            int continuousDays,
            Set<Long> claimedIds
    ) {
        return rules.stream()
                .filter(rule -> !claimedIds.contains(rule.getId()))
                .filter(rule -> continuousDays < rule.getRequiredDays())
                .min(Comparator.comparingInt(SignRewardRule::getRequiredDays))
                .map(rule -> SignCalendarVO.NextMilestoneVO.builder()
                        .targetDays(rule.getRequiredDays())
                        .remainDays(rule.getRequiredDays() - continuousDays)
                        .rewardName(rule.getRewardName())
                        .rewardIcon(rule.getRewardIcon())
                        .build())
                .orElse(null);
    }

    public SignResultVO.MilestoneVO checkNewMilestoneUnlock(Long userId, int continuousDays, String cycleMonth, List<SignRewardRule> rules) {
        Set<Long> claimedIds = rewardRecordMapper.selectClaimedRuleIds(userId, cycleMonth);

        return rules.stream()
                .filter(rule -> rule.getRequiredDays() == continuousDays)
                .filter(rule -> !claimedIds.contains(rule.getId()))
                .findFirst()
                .map(rule -> SignResultVO.MilestoneVO.builder()
                        .ruleId(rule.getId())
                        .requiredDays(rule.getRequiredDays())
                        .rewardName(rule.getRewardName())
                        .rewardIcon(rule.getRewardIcon())
                        .message(String.format("恭喜解锁「连续签到%d天」成就，请领取%s！",
                                rule.getRequiredDays(), rule.getRewardName()))
                        .build())
                .orElse(null);
    }

    public SignResultVO.MilestoneVO checkMilestonesInRange(
            Long userId,
            int from,
            int to,
            String cycleMonth,
            List<SignRewardRule> rules
    ) {
        Set<Long> claimedIds = rewardRecordMapper.selectClaimedRuleIds(userId, cycleMonth);

        return rules.stream()
                .filter(rule -> rule.getRequiredDays() >= from && rule.getRequiredDays() <= to)
                .filter(rule -> !claimedIds.contains(rule.getId()))
                .max(Comparator.comparingInt(SignRewardRule::getRequiredDays))
                .map(rule -> SignResultVO.MilestoneVO.builder()
                        .ruleId(rule.getId())
                        .requiredDays(rule.getRequiredDays())
                        .rewardName(rule.getRewardName())
                        .rewardIcon(rule.getRewardIcon())
                        .message(String.format("补签修复了连续签到链！解锁「连续%d天」成就", rule.getRequiredDays()))
                        .build())
                .orElse(null);
    }

    public Object grantReward(Long userId, SignRewardRule rule) {
        return switch (rule.getRewardType()) {
            case 1 -> Map.of(
                    "lotteryChances", rule.getRewardValue(),
                    "lotteryUrl", "/pages/lottery/index"
            );
            case 2 -> Map.of(
                    "voucherId", rule.getVoucherId() != null ? rule.getVoucherId() : 0,
                    "voucherTitle", rule.getRewardName()
            );
            case 3 -> {
                userInfoMapper.addPointsSafe(userId, rule.getRewardValue());
                Integer newPoints = userInfoMapper.getPoints(userId);
                yield signPointsSupport.buildPointsRewardDetail(rule.getRewardValue(), newPoints);
            }
            default -> Map.of();
        };
    }

    public String buildClaimMessage(SignRewardRule rule) {
        return switch (rule.getRewardType()) {
            case 1 -> String.format("获得%d次抽奖机会，快去试试手气吧！", rule.getRewardValue());
            case 2 -> String.format("%s已发放至您的账户，请在「我的优惠券」中查看", rule.getRewardName());
            case 3 -> String.format("%d积分已到账！", rule.getRewardValue());
            default -> "奖励已发放！";
        };
    }

    public String getRewardTypeDesc(int rewardType) {
        return switch (rewardType) {
            case 1 -> "抽奖机会";
            case 2 -> "优惠券";
            case 3 -> "积分";
            case 4 -> "实物";
            default -> "未知";
        };
    }

    public String resolveSignAnimation(SignResultVO.MilestoneVO unlockedMilestone, int signDays, int monthDays) {
        if (unlockedMilestone != null) {
            return "milestone";
        }
        if (signDays == monthDays) {
            return "month_complete";
        }
        return "normal";
    }

    public String buildCycleMonth(int year, int month) {
        return String.format("%d-%02d", year, month);
    }
}
