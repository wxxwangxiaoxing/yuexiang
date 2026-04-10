package com.yuexiang.user.service.impl;

import com.yuexiang.common.exception.BusinessException;
import com.yuexiang.user.constant.SignConstants;
import com.yuexiang.user.domain.dto.RepairSignDTO;
import com.yuexiang.user.domain.entity.Sign;
import com.yuexiang.user.domain.entity.SignRepairRecord;
import com.yuexiang.user.domain.entity.SignRewardRecord;
import com.yuexiang.user.domain.entity.SignRewardRule;
import com.yuexiang.user.domain.vo.ClaimResultVO;
import com.yuexiang.user.domain.vo.RepairResultVO;
import com.yuexiang.user.domain.vo.SignCalendarVO;
import com.yuexiang.user.domain.vo.SignRankVO;
import com.yuexiang.user.domain.vo.SignResultVO;
import com.yuexiang.user.domain.vo.SignRewardsVO;
import com.yuexiang.user.mapper.SignMapper;
import com.yuexiang.user.mapper.SignRepairRecordMapper;
import com.yuexiang.user.mapper.SignRewardRecordMapper;
import com.yuexiang.user.mapper.UserInfoMapper;
import com.yuexiang.user.service.SignService;
import com.yuexiang.user.support.SignCacheSupport;
import com.yuexiang.user.support.SignComputeSupport;
import com.yuexiang.user.support.SignPointsSupport;
import com.yuexiang.user.support.SignQuerySupport;
import com.yuexiang.user.support.SignRewardSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 签到服务实现类
 * <p>
 * 功能包括：
 * 1. 获取签到日历
 * 2. 每日签到
 * 3. 补签
 * 4. 查询签到奖励规则
 * 5. 领取里程碑奖励
 * 6. 查询签到排行榜（当前为预留实现）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SignServiceImpl implements SignService {

    private final SignMapper signMapper;
    private final SignRewardRecordMapper rewardRecordMapper;
    private final SignRepairRecordMapper repairRecordMapper;
    private final UserInfoMapper userInfoMapper;
    private final SignCacheSupport signCacheSupport;
    private final SignQuerySupport signQuerySupport;
    private final SignComputeSupport signComputeSupport;
    private final SignPointsSupport signPointsSupport;
    private final SignRewardSupport signRewardSupport;

    @Override
    public SignCalendarVO getCalendar(Long userId, Integer year, Integer month) {
        LocalDate today = LocalDate.now(SignConstants.ZONE);
        int targetYear = year != null ? year : today.getYear();
        int targetMonth = month != null ? month : today.getMonthValue();

        LocalDate firstDayOfMonth = LocalDate.of(targetYear, targetMonth, 1);
        int totalDaysInMonth = firstDayOfMonth.lengthOfMonth();
        int firstDayOfWeek = firstDayOfMonth.getDayOfWeek().getValue();

        Sign sign = signMapper.selectByUserMonth(userId, targetYear, targetMonth);
        int bitmap = sign != null ? sign.getSignBitmap() : 0;

        List<Integer> signedDays = signComputeSupport.parseSignedDays(bitmap, totalDaysInMonth);
        List<Integer> repairedDays = signQuerySupport.getRepairedDays(userId, targetYear, targetMonth);

        int continuousDays = signComputeSupport.calcCurrentContinuousDays(userId, today, targetYear, targetMonth, bitmap);
        boolean isSignedToday = signComputeSupport.isCurrentMonth(targetYear, targetMonth, today)
                && signComputeSupport.isSigned(bitmap, today.getDayOfMonth());

        int maxContinuousDays = signQuerySupport.getMaxContinuousSignDays(userId);

        int todayPoints = signPointsSupport.getBasePoints(isSignedToday ? continuousDays : continuousDays + 1);
        int monthTotalPoints = signPointsSupport.getMonthTotalPoints(userId, targetYear, targetMonth);

        // 当前设计：补签剩余次数显示“当前自然月”剩余次数
        int usedRepair = repairRecordMapper.countByUserMonth(userId, today.getYear(), today.getMonthValue());
        int repairRemain = Math.max(0, SignConstants.REPAIR_MAX_PER_MONTH - usedRepair);

        List<SignRewardRule> rules = signQuerySupport.getEnabledRewardRules();
        String cycleMonth = signRewardSupport.buildCycleMonth(today.getYear(), today.getMonthValue());
        Set<Long> claimedIds = rewardRecordMapper.selectClaimedRuleIds(userId, cycleMonth);

        int finalContinuousDays = continuousDays;
        List<SignCalendarVO.MilestoneStatusVO> milestones = rules.stream()
                .map(rule -> {
                    int status = signRewardSupport.determineMilestoneStatus(
                            rule.getRequiredDays(),
                            rule.getId(),
                            finalContinuousDays,
                            claimedIds
                    );
                    return SignCalendarVO.MilestoneStatusVO.builder()
                            .ruleId(rule.getId())
                            .requiredDays(rule.getRequiredDays())
                            .rewardType(rule.getRewardType())
                            .rewardName(rule.getRewardName())
                            .rewardIcon(rule.getRewardIcon())
                            .status(status)
                            .statusDesc(signRewardSupport.getStatusDesc(status))
                            .build();
                })
                .toList();

        SignCalendarVO.NextMilestoneVO nextMilestone = signRewardSupport.findNextMilestone(rules, finalContinuousDays, claimedIds);

        return SignCalendarVO.builder()
                .year(targetYear)
                .month(targetMonth)
                .today(signComputeSupport.isCurrentMonth(targetYear, targetMonth, today) ? today.getDayOfMonth() : null)
                .totalDaysInMonth(totalDaysInMonth)
                .firstDayOfWeek(firstDayOfWeek)
                .signedDays(signedDays)
                .repairedDays(repairedDays)
                .signedCount(signedDays.size())
                .continuousDays(continuousDays)
                .maxContinuousDays(maxContinuousDays)
                .isSignedToday(isSignedToday)
                .todayPoints(todayPoints)
                .monthTotalPoints(monthTotalPoints)
                .repairRemain(repairRemain)
                .repairCost(SignConstants.REPAIR_COST_POINTS)
                .nextMilestone(nextMilestone)
                .milestones(milestones)
                .serverTime(System.currentTimeMillis())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SignResultVO doSign(Long userId) {
        LocalDate today = LocalDate.now(SignConstants.ZONE);
        int year = today.getYear();
        int month = today.getMonthValue();
        int day = today.getDayOfMonth();

        String lockKey = signCacheSupport.buildSignLockKey(userId);
        String lockValue = signCacheSupport.tryLock(lockKey);
        if (lockValue == null) {
            throw new BusinessException(SignConstants.CODE_TOO_FREQUENT, "操作频繁，请稍后重试");
        }

        try {
            Sign sign = signQuerySupport.getOrCreateSign(userId, year, month);
            int bitmap = sign.getSignBitmap();

            if (signComputeSupport.isSigned(bitmap, day)) {
                throw new BusinessException(40020, "今日已签到，明天再来吧");
            }

            int continuousDays = signComputeSupport.calcContinuousDaysEndingAt(userId, today.minusDays(1)) + 1;
            int basePoints = signPointsSupport.getBasePoints(continuousDays);
            int bonusPoints = signPointsSupport.getBonusPoints(continuousDays);
            int totalReward = basePoints + bonusPoints;

            int newBitmap = signComputeSupport.markSigned(bitmap, day);
            int newSignDays = sign.getSignDays() + 1;
            signMapper.updateBitmap(sign.getId(), newBitmap, newSignDays);

            userInfoMapper.addPointsSafe(userId, totalReward);
            Integer userTotalPoints = userInfoMapper.getPoints(userId);
            if (userTotalPoints == null) {
                userTotalPoints = 0;
            }

            userInfoMapper.updateMaxContinuousIfGreater(userId, continuousDays);

            signPointsSupport.savePointsRecord(
                    userId,
                    totalReward,
                    userTotalPoints,
                    "SIGN",
                    sign.getId(),
                    signPointsSupport.buildSignDesc(basePoints, bonusPoints, continuousDays)
            );

            String cycleMonth = signRewardSupport.buildCycleMonth(year, month);
            SignResultVO.MilestoneVO unlockedMilestone = signRewardSupport.checkNewMilestoneUnlock(
                    userId,
                    continuousDays,
                    cycleMonth,
                    signQuerySupport.getEnabledRewardRules()
            );

            signCacheSupport.updateSignCache(userId, year, month, newBitmap, continuousDays);

            String animation = signRewardSupport.resolveSignAnimation(unlockedMilestone, newSignDays, today.lengthOfMonth());

            return SignResultVO.builder()
                    .signDate(today.toString())
                    .day(day)
                    .basePoints(basePoints)
                    .bonusPoints(bonusPoints)
                    .totalRewardPoints(totalReward)
                    .userTotalPoints(userTotalPoints)
                    .continuousDays(continuousDays)
                    .isBonus(bonusPoints > 0)
                    .bonusDesc(bonusPoints > 0
                            ? String.format("🎉 连续签到%d天！额外奖励%d积分", continuousDays, bonusPoints)
                            : null)
                    .unlockedMilestone(unlockedMilestone)
                    .animation(animation)
                    .build();
        } finally {
            signCacheSupport.unlock(lockKey, lockValue);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RepairResultVO repairSign(Long userId, RepairSignDTO dto) {
        LocalDate repairDate = LocalDate.of(dto.getYear(), dto.getMonth(), dto.getDay());
        LocalDate today = LocalDate.now(SignConstants.ZONE);

        String lockKey = signCacheSupport.buildRepairLockKey(userId);
        String lockValue = signCacheSupport.tryLock(lockKey);
        if (lockValue == null) {
            throw new BusinessException(SignConstants.CODE_TOO_FREQUENT, "操作频繁，请稍后重试");
        }

        try {
            signComputeSupport.validateRepairDate(repairDate, today);

            int usedRepair = repairRecordMapper.countByUserMonth(userId, today.getYear(), today.getMonthValue());
            if (usedRepair >= SignConstants.REPAIR_MAX_PER_MONTH) {
                throw new BusinessException(40025, String.format("本月补签次数已达上限（%d次）", SignConstants.REPAIR_MAX_PER_MONTH));
            }

            Integer currentPoints = userInfoMapper.getPoints(userId);
            if (currentPoints == null) {
                currentPoints = 0;
            }
            if (currentPoints < SignConstants.REPAIR_COST_POINTS) {
                throw new BusinessException(
                        40022,
                        String.format("积分不足，补签需消耗%d积分，当前仅%d积分", SignConstants.REPAIR_COST_POINTS, currentPoints)
                );
            }

            int repairYear = repairDate.getYear();
            int repairMonth = repairDate.getMonthValue();
            int repairDay = repairDate.getDayOfMonth();

            Sign sign = signQuerySupport.getUserMonthSign(userId, repairYear, repairMonth);
            if (sign != null && signComputeSupport.isSigned(sign.getSignBitmap(), repairDay)) {
                throw new BusinessException(40021, "该日期已签到，无需补签");
            }

            userInfoMapper.addPointsSafe(userId, -SignConstants.REPAIR_COST_POINTS);
            int remainPoints = currentPoints - SignConstants.REPAIR_COST_POINTS;

            sign = signQuerySupport.getOrCreateSign(userId, repairYear, repairMonth);
            int newBitmap = signComputeSupport.markSigned(sign.getSignBitmap(), repairDay);
            signMapper.updateBitmap(sign.getId(), newBitmap, sign.getSignDays() + 1);

            SignRepairRecord repairRecord = new SignRepairRecord();
            repairRecord.setUserId(userId);
            repairRecord.setRepairDate(repairDate);
            repairRecord.setCostPoints(SignConstants.REPAIR_COST_POINTS);
            repairRecordMapper.insert(repairRecord);

            signPointsSupport.savePointsRecord(
                    userId,
                    -SignConstants.REPAIR_COST_POINTS,
                    remainPoints,
                    "SIGN_REPAIR",
                    repairRecord.getId(),
                    String.format("补签%s消耗%d积分", repairDate, SignConstants.REPAIR_COST_POINTS)
            );

            int prevContinuous = signComputeSupport.calcContinuousDaysEndingAt(userId, today.minusDays(1));
            boolean signedToday = signQuerySupport.isSignedToday(userId, today);
            int newContinuous = signedToday
                    ? signComputeSupport.calcContinuousDaysEndingAt(userId, today)
                    : prevContinuous;

            userInfoMapper.updateMaxContinuousIfGreater(userId, newContinuous);

            String cycleMonth = signRewardSupport.buildCycleMonth(today.getYear(), today.getMonthValue());
            SignResultVO.MilestoneVO milestone = null;
            if (newContinuous > prevContinuous) {
                milestone = signRewardSupport.checkMilestonesInRange(
                        userId,
                        prevContinuous + 1,
                        newContinuous,
                        cycleMonth,
                        signQuerySupport.getEnabledRewardRules()
                );
            }

            signCacheSupport.evictSignCache(userId, repairYear, repairMonth);
            signCacheSupport.updateContinuousCache(userId, newContinuous);

            return RepairResultVO.builder()
                    .signDate(repairDate.toString())
                    .costPoints(SignConstants.REPAIR_COST_POINTS)
                    .remainPoints(remainPoints)
                    .continuousDays(newContinuous)
                    .continuousChanged(newContinuous != prevContinuous)
                    .unlockedMilestone(milestone)
                    .build();
        } finally {
            signCacheSupport.unlock(lockKey, lockValue);
        }
    }

    @Override
    public SignRewardsVO getRewards() {
        List<SignRewardRule> rules = signQuerySupport.getEnabledRewardRules();

        List<SignRewardsVO.DailyPointsRuleVO> dailyPointsRules = List.of(
                SignRewardsVO.DailyPointsRuleVO.builder().minDays(1).maxDays(2).points(5).desc("签到第1-2天，每天+5积分").build(),
                SignRewardsVO.DailyPointsRuleVO.builder().minDays(3).maxDays(4).points(10).desc("连续3-4天，每天+10积分").build(),
                SignRewardsVO.DailyPointsRuleVO.builder().minDays(5).maxDays(6).points(15).desc("连续5-6天，每天+15积分").build(),
                SignRewardsVO.DailyPointsRuleVO.builder().minDays(7).maxDays(999).points(20).desc("连续7天及以上，每天+20积分").build()
        );

        List<SignRewardsVO.MilestoneRuleVO> milestoneRules = rules.stream()
                .map(rule -> SignRewardsVO.MilestoneRuleVO.builder()
                        .ruleId(rule.getId())
                        .requiredDays(rule.getRequiredDays())
                        .rewardType(rule.getRewardType())
                        .rewardTypeDesc(signRewardSupport.getRewardTypeDesc(rule.getRewardType()))
                        .rewardName(rule.getRewardName())
                        .rewardIcon(rule.getRewardIcon())
                        .rewardValue(rule.getRewardValue())
                        .bonusPoints(rule.getBonusPoints())
                        .description(rule.getDescription())
                        .build())
                .toList();

        SignRewardsVO.RepairRulesVO repairRules = SignRewardsVO.RepairRulesVO.builder()
                .costPoints(SignConstants.REPAIR_COST_POINTS)
                .maxPerMonth(SignConstants.REPAIR_MAX_PER_MONTH)
                .repairWindowDays(SignConstants.REPAIR_WINDOW_DAYS)
                .description(String.format("可补签最近%d天，每次消耗%d积分，每月最多%d次",
                        SignConstants.REPAIR_WINDOW_DAYS, SignConstants.REPAIR_COST_POINTS, SignConstants.REPAIR_MAX_PER_MONTH))
                .build();

        return SignRewardsVO.builder()
                .dailyPointsRules(dailyPointsRules)
                .milestoneRules(milestoneRules)
                .repairRules(repairRules)
                .cycleDesc("里程碑奖励每自然月重置，连续天数跨月累计")
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClaimResultVO claimReward(Long userId, Long ruleId) {
        String lockKey = signCacheSupport.buildClaimLockKey(userId, ruleId);
        String lockValue = signCacheSupport.tryLock(lockKey);
        if (lockValue == null) {
            throw new BusinessException(SignConstants.CODE_TOO_FREQUENT, "操作频繁，请稍后重试");
        }

        try {
            SignRewardRule rule = signQuerySupport.getRewardRule(ruleId);
            if (rule == null || rule.getStatus() != 1) {
                throw new BusinessException(40032, "奖励活动已结束");
            }

            LocalDate today = LocalDate.now(SignConstants.ZONE);
            String cycleMonth = signRewardSupport.buildCycleMonth(today.getYear(), today.getMonthValue());

            boolean claimed = rewardRecordMapper.existsByUserRuleCycle(userId, ruleId, cycleMonth);
            if (claimed) {
                throw new BusinessException(40031, "该奖励已领取，不可重复领取");
            }

            int continuousDays = signComputeSupport.calcContinuousDaysEndingAt(userId, today);
            if (continuousDays < rule.getRequiredDays()) {
                throw new BusinessException(
                        40030,
                        String.format("连续签到天数未达要求（需连续%d天，当前%d天）", rule.getRequiredDays(), continuousDays)
                );
            }

            Object rewardDetail = signRewardSupport.grantReward(userId, rule);

            SignRewardRecord record = new SignRewardRecord();
            record.setUserId(userId);
            record.setRuleId(ruleId);
            record.setCycleMonth(cycleMonth);
            record.setRewardType(rule.getRewardType());
            record.setRewardValue(rule.getRewardValue());
            record.setStatus(1);
            record.setBizId(null);
            rewardRecordMapper.insert(record);

            return ClaimResultVO.builder()
                    .ruleId(ruleId)
                    .rewardType(rule.getRewardType())
                    .rewardName(rule.getRewardName())
                    .rewardDetail(rewardDetail)
                    .message(signRewardSupport.buildClaimMessage(rule))
                    .build();
        } finally {
            signCacheSupport.unlock(lockKey, lockValue);
        }
    }

    @Override
    public SignRankVO getRank(Integer type, Integer top) {
        int rankType = type != null ? type : 1;
        int rankTop = top != null ? top : 20;
        rankTop = Math.min(rankTop, 50);

        String rankTypeDesc = rankType == 1 ? "本月签到排行" : "连续签到排行";

        // TODO: 后续可基于 Redis ZSet / 数据库统计实现真实排行榜
        return SignRankVO.builder()
                .rankType(rankType)
                .rankTypeDesc(rankTypeDesc)
                .updateTime(System.currentTimeMillis())
                .myRank(null)
                .list(new ArrayList<>())
                .build();
    }
}
