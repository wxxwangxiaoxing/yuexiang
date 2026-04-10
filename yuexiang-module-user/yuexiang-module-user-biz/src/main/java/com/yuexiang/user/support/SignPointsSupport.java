package com.yuexiang.user.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuexiang.user.domain.entity.PointsRecord;
import com.yuexiang.user.domain.entity.SignRewardRule;
import com.yuexiang.user.mapper.PointsRecordMapper;
import com.yuexiang.user.mapper.SignRewardRuleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SignPointsSupport {

    private final PointsRecordMapper pointsRecordMapper;
    private final SignRewardRuleMapper rewardRuleMapper;

    public int getBasePoints(int continuousDays) {
        if (continuousDays >= 7) {
            return 20;
        }
        if (continuousDays >= 5) {
            return 15;
        }
        if (continuousDays >= 3) {
            return 10;
        }
        return 5;
    }

    public int getBonusPoints(int continuousDays) {
        SignRewardRule rule = rewardRuleMapper.selectByRequiredDays(continuousDays);
        return rule != null ? rule.getBonusPoints() : 0;
    }

    public int getMonthTotalPoints(Long userId, int year, int month) {
        LocalDateTime begin = LocalDate.of(year, month, 1).atStartOfDay();
        LocalDateTime end = begin.plusMonths(1);

        LambdaQueryWrapper<PointsRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointsRecord::getUserId, userId)
                .ge(PointsRecord::getCreateTime, begin)
                .lt(PointsRecord::getCreateTime, end)
                .in(PointsRecord::getBizType, List.of("SIGN", "SIGN_REPAIR"));

        List<PointsRecord> records = pointsRecordMapper.selectList(wrapper);
        if (records == null || records.isEmpty()) {
            return 0;
        }

        return records.stream()
                .mapToInt(PointsRecord::getPoints)
                .sum();
    }

    public void savePointsRecord(Long userId, int points, int balance, String bizType, Long bizId, String description) {
        PointsRecord record = new PointsRecord();
        record.setUserId(userId);
        record.setType(points >= 0 ? 1 : 7);
        record.setPoints(points);
        record.setBalance(balance);
        record.setBizType(bizType);
        record.setBizId(bizId);
        record.setDescription(description);
        pointsRecordMapper.insert(record);
    }

    public String buildSignDesc(int basePoints, int bonusPoints, int continuousDays) {
        if (bonusPoints > 0) {
            return String.format("连续签到%d天，基础%d+额外%d积分", continuousDays, basePoints, bonusPoints);
        }
        return String.format("签到获得%d积分", basePoints);
    }

    public Map<String, Object> buildPointsRewardDetail(int rewardValue, Integer totalPoints) {
        return Map.of(
                "points", rewardValue,
                "totalPoints", totalPoints
        );
    }
}
